import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Main {

    static final int PORT = 1234;
    static final String hostname = "localhost";

    static final Set<SocketChannel> clients = new HashSet<>();

    public static void main(String[] args) {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(hostname, PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();

                    if (selectionKey.isAcceptable()) {
                        SocketChannel client = serverSocketChannel.accept();
                        System.out.println(client);
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        System.out.println("Accepted connection from: " + client.getRemoteAddress());
                        clients.add(client);
                    }
                    if (selectionKey.isReadable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        SocketChannel client = (SocketChannel) selectionKey.channel();

                        client.read(buffer);
                        for (SocketChannel clientChannel : clients) {
                            if (clientChannel != client) {
                                buffer.rewind();
                                clientChannel.write(buffer);
                            }
                        }

                        buffer.flip();  // Switch the buffer from writing to reading mode
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);  // Copy data from buffer into the byte array
                        System.out.println("Received: " + new String(data).trim());
                        buffer.clear();
                    }
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
