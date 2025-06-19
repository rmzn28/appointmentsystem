import javax.swing.*;

public class AdminAddUserFrame extends JFrame {
    private JTextField usernameField;
    private JTextField nameField; // Yeni alan
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton addButton;

    public AdminAddUserFrame() {
        setTitle("Kullanıcı Ekle");
        setSize(300, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        usernameField = new JTextField(15);
        nameField = new JTextField(15); // Ad kısmı
        passwordField = new JPasswordField(15);
        roleComboBox = new JComboBox<>(new String[]{"student", "instructor"});
        addButton = new JButton("Kullanıcı Ekle");

        panel.add(new JLabel("Kullanıcı Adı:"));
        panel.add(usernameField);
        panel.add(new JLabel("Ad Soyad:"));
        panel.add(nameField);
        panel.add(new JLabel("Şifre:"));
        panel.add(passwordField);
        panel.add(new JLabel("Rol:"));
        panel.add(roleComboBox);
        panel.add(addButton);

        addButton.addActionListener(e -> addUser());

        setVisible(true);
    }

    private void addUser() {
        String username = usernameField.getText();
        String name = nameField.getText();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty() || name.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tüm alanları doldurun.");
            return;
        }

        UserDAO userDAO = new UserDAO();

        if ("student".equals(role)) {
            userDAO.addUser(new Student(0,   username, name, password));
        } else if ("instructor".equals(role)) {
            userDAO.addUser(new Instructor(0, username, name, password));
        }

        JOptionPane.showMessageDialog(this, "Kullanıcı başarıyla eklendi!");
        usernameField.setText("");
        nameField.setText("");
        passwordField.setText("");
    }
}

