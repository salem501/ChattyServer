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

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(hostname, PORT));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (SelectionKey selectionKey : selectionKeys) {
                if (selectionKey.isAcceptable()) {
                    handleAccept(serverSocketChannel, selector);
                }
                if (selectionKey.isReadable()) {
                    handleRead(selectionKey);
                }
            }
            selectionKeys.clear();
        }
    }

    private static void handleAccept(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        SocketChannel client = serverSocketChannel.accept();
        System.out.println(client);
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("Accepted connection from: " + client.getRemoteAddress());
        clients.add(client);
    }

    private static void handleRead(SelectionKey selectionKey) {
        SocketChannel client = (SocketChannel) selectionKey.channel();
        ByteBuffer buffer = ByteBuffer.allocate(256);

        try {
            client.read(buffer);
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            System.out.println("Received: " + new String(data).trim());

            for (SocketChannel otherClient : clients) {
                if (otherClient != client) {
                    buffer.rewind();
                    otherClient.write(buffer);
                }
            }
            buffer.clear();
        } catch (IOException e) {
            System.out.println("Client disconnected");
            try {
                buffer.clear();
                clients.remove(client);
                client.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
