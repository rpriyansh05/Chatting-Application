import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Client implements ActionListener {

    private JTextField text;
    private static JPanel a1;
    private JScrollPane scrollPane;
    private static JFrame f = new JFrame();

    private static DataOutputStream dout;

    public Client() {
        f.setLayout(null);

        // Header
        JPanel p1 = new JPanel();
        p1.setBackground(new Color(7, 94, 84));
        p1.setBounds(0, 0, 450, 70);
        p1.setLayout(null);
        f.add(p1);

        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icons/3.png"));
        Image i2 = i1.getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT);
        JLabel back = new JLabel(new ImageIcon(i2));
        back.setBounds(5, 20, 25, 25);
        p1.add(back);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent ae) { System.exit(0); }
        });

        ImageIcon i4 = new ImageIcon(ClassLoader.getSystemResource("icons/2.jpg"));
        Image i5 = i4.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT);
        JLabel profile = new JLabel(new ImageIcon(i5));
        profile.setBounds(40, 10, 50, 50);
        p1.add(profile);

        ImageIcon i7 = new ImageIcon(ClassLoader.getSystemResource("icons/video.png"));
        Image i8 = i7.getImage().getScaledInstance(30, 30, Image.SCALE_DEFAULT);
        JLabel video = new JLabel(new ImageIcon(i8));
        video.setBounds(300, 20, 30, 30);
        p1.add(video);

        ImageIcon i10 = new ImageIcon(ClassLoader.getSystemResource("icons/phone.png"));
        Image i11 = i10.getImage().getScaledInstance(35, 30, Image.SCALE_DEFAULT);
        JLabel phone = new JLabel(new ImageIcon(i11));
        phone.setBounds(360, 20, 35, 30);
        p1.add(phone);

        ImageIcon i13 = new ImageIcon(ClassLoader.getSystemResource("icons/3icon.png"));
        Image i14 = i13.getImage().getScaledInstance(10, 25, Image.SCALE_DEFAULT);
        JLabel morevert = new JLabel(new ImageIcon(i14));
        morevert.setBounds(420, 20, 10, 25);
        p1.add(morevert);

        JLabel name = new JLabel("Immu");
        name.setBounds(110, 15, 200, 18);
        name.setForeground(Color.WHITE);
        name.setFont(new Font("SAN_SERIF", Font.BOLD, 18));
        p1.add(name);

        JLabel status = new JLabel("Active Now");
        status.setBounds(110, 35, 200, 18);
        status.setForeground(Color.WHITE);
        status.setFont(new Font("SAN_SERIF", Font.PLAIN, 14));
        p1.add(status);

        // Chat area (scrollable)
        a1 = new JPanel();
        a1.setLayout(new BoxLayout(a1, BoxLayout.Y_AXIS));
        a1.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(a1);
        scrollPane.setBounds(5, 75, 440, 570);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        f.add(scrollPane);

        // Input
        text = new JTextField();
        text.setBounds(5, 655, 310, 40);
        text.setFont(new Font("SAN_SERIF", Font.PLAIN, 16));
        f.add(text);
        text.addActionListener(this);

        JButton send = new JButton("Send");
        send.setBounds(320, 655, 123, 40);
        send.setBackground(new Color(7, 94, 84));
        send.setForeground(Color.WHITE);
        send.setFont(new Font("SAN_SERIF", Font.PLAIN, 16));
        send.addActionListener(this);
        f.add(send);

        f.setSize(450, 700);
        f.setLocation(700, 50); // different location so server + client windows don't overlap
        f.setUndecorated(true);
        f.getContentPane().setBackground(Color.WHITE);
        f.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        try {
            String out = text.getText().trim();
            if (out.isEmpty()) return;

            JPanel p2 = formatLabel(out);

            JPanel right = new JPanel(new BorderLayout());
            right.setOpaque(false);
            right.add(p2, BorderLayout.LINE_END);

            a1.add(right);
            a1.add(Box.createVerticalStrut(15));
            a1.revalidate();
            autoScrollToBottom();

            if (dout != null) {
                dout.writeUTF(out);
            }

            text.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void autoScrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    public static JPanel formatLabel(String out) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel output = new JLabel("<html><p style=\"width: 150px;\">" + escapeHtml(out) + "</p></html>");
        output.setFont(new Font("Tahoma", Font.PLAIN, 16));
        output.setBackground(new Color(37, 211, 102));
        output.setOpaque(true);
        output.setBorder(new EmptyBorder(15, 15, 15, 50));

        panel.add(output);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        JLabel time = new JLabel(sdf.format(cal.getTime()));
        time.setFont(new Font("Tahoma", Font.PLAIN, 12));
        time.setBorder(new EmptyBorder(5, 5, 0, 0));
        panel.add(time);

        return panel;
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);

        new Thread(() -> {
            try {
                Socket s = new Socket("127.0.0.1", 6001); // connect to server
                DataInputStream din = new DataInputStream(s.getInputStream());
                dout = new DataOutputStream(s.getOutputStream());

                while (true) {
                    String msg = din.readUTF();

                    JPanel panel = formatLabel(msg);
                    JPanel left = new JPanel(new BorderLayout());
                    left.setOpaque(false);
                    left.add(panel, BorderLayout.LINE_START);

                    SwingUtilities.invokeLater(() -> {
                        a1.add(left);
                        a1.add(Box.createVerticalStrut(15));
                        a1.revalidate();
                        JScrollBar bar = ((JScrollPane) f.getContentPane().getComponent(1))
                                .getVerticalScrollBar();
                        bar.setValue(bar.getMaximum());
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Client-Reader").start();
    }
}
