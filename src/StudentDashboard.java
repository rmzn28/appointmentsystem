import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

// Öğrenci panelini temsil eden Swing arayüz sınıfı
public class StudentDashboard extends JFrame {
    private final int studentId;
    private final JComboBox<Instructor> instructorBox; // Öğretmen seçim kutusu
    private final JDateChooser dateChooser; // Tarih seçici (takvim)
    private final JComboBox<String> startTimeComboBox; // Başlangıç saati seçim kutusu
    private final JPanel appointmentsPanel; // Randevuların listelendiği panel


    public StudentDashboard(int studentId) {
        this.studentId = studentId;
        setTitle("Öğrenci Paneli");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // Ortada açılması için
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        instructorBox = new JComboBox<>();
        loadInstructors();// Veritabanından öğretmenleri yükle
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");

        // Öğretmen veya tarih değiştiğinde uygun saatleri tekrar yükle
        instructorBox.addActionListener(e -> loadAvailableTimes());
        dateChooser.addPropertyChangeListener("date", e -> loadAvailableTimes());

        startTimeComboBox = new JComboBox<>();

        // Randevu talep butonu
        JButton requestButton = getJButton();
        // Üst panele öğeler eklenir (öğretmen ve tarih seçimi)
        topPanel.add(new JLabel("Hoca: "));
        topPanel.add(instructorBox);
        topPanel.add(new JLabel("Tarih: "));
        topPanel.add(dateChooser);

        add(topPanel, BorderLayout.NORTH);

        // Merkez panel ve alt kontroller
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(new JLabel("Başlangıç Saati: "));
        bottomPanel.add(startTimeComboBox);
        bottomPanel.add(requestButton);

        // Randevuları görüntüle butonu
        JButton myAppointmentsButton = new JButton("Randevularım");
        myAppointmentsButton.addActionListener(e -> showAppointments()); // Randevuları listele
        bottomPanel.add(myAppointmentsButton);

        centerPanel.add(bottomPanel, BorderLayout.NORTH);

        appointmentsPanel = new JPanel();
        appointmentsPanel.setLayout(new BoxLayout(appointmentsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(appointmentsPanel);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // Öğrenci randevu talebi gönderdiğinde veritabanına kayıt işlemi
    private void requestAppointment(String selectedStartTime) throws SQLException {
        Date selectedDate = dateChooser.getDate();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
        Instructor selectedInstructor = (Instructor) instructorBox.getSelectedItem();
        assert selectedInstructor != null;
        int instructorId = selectedInstructor.getId();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO appointments (student_id, instructor_id, date, start_time, end_time, status) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            stmt.setInt(2, instructorId);
            stmt.setString(3, date);
            stmt.setString(4, selectedStartTime);

            // 20 dakika sonrası hesaplanıyor
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date startTime = sdf.parse(selectedStartTime);
            long endMillis = startTime.getTime() + 20 * 60 * 1000;
            String endTime = sdf.format(new Date(endMillis));

            stmt.setString(5, endTime); //
            stmt.setString(6, "Beklemede");
           stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Randevu talebiniz başarıyla gönderildi.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Randevu kaydedilirken hata oluştu.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Öğrencinin mevcut randevularını gösteren metot
    private void showAppointments() {
        appointmentsPanel.removeAll();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT a.id, a.date, a.start_time, a.end_time, a.status, i.name AS instructor_name " +
                    "FROM appointments a " +
                    "JOIN instructors i ON a.instructor_id = i.id " +
                    "WHERE a.student_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int appointmentId = rs.getInt("id");
                String date = rs.getString("date");
                String startTime = rs.getString("start_time");
                String endTime = rs.getString("end_time");
                String status = rs.getString("status");
                String instructorName = rs.getString("instructor_name");

                JPanel appointmentPanel = new JPanel();
                appointmentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                appointmentPanel.add(new JLabel("Tarih: " + date +
                        " Saat: " + startTime + " - " + endTime +
                        " Eğitmen: " + instructorName +
                        " Durum: " + status));

                if (status.equals("Beklemede")) {
                    JButton cancelButton = new JButton("İptal Et");
                    cancelButton.addActionListener(e -> {
                        try {
                            cancelAppointment(appointmentId);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(this, "Randevu iptal edilirken hata oluştu.", "Hata", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    appointmentPanel.add(cancelButton);
                }

                appointmentsPanel.add(appointmentPanel);
            }

            appointmentsPanel.revalidate();
            appointmentsPanel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Randevular yüklenirken hata oluştu.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Randevuyu iptal eder ve durumunu günceller
    private void cancelAppointment(int appointmentId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE appointments SET status = 'İptal Edildi' WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, appointmentId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Randevunuz iptal edildi.");
            showAppointments();
        }
    }


    private JButton getJButton() {
        JButton requestButton = new JButton("Randevu Talep Et");


        // Randevu talep butonuna basıldığında randevu veritabanına eklenir
        requestButton.addActionListener(e -> {
            String selectedStartTime = (String) startTimeComboBox.getSelectedItem();

            if (selectedStartTime == null) {
                JOptionPane.showMessageDialog(this, "Lütfen hem başlangıç saatini seçin.", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                requestAppointment(selectedStartTime);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Randevu talebi gönderilemedi.", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });
        return requestButton;
    }


    // Veritabanından öğretmenleri çek ve seçim kutusuna ekle
    private void loadInstructors() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id , username,name , password FROM instructors";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String name = rs.getString("name");
                String password = rs.getString("password");

                instructorBox.addItem(new Instructor(id, username, name, password));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Hocalar yüklenemedi.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Seçilen hocanın ve tarihin uygun saatlerini yükle
    private void loadAvailableTimes() {
        startTimeComboBox.removeAllItems();
        Date selectedDate = dateChooser.getDate();
        Instructor selectedInstructor = (Instructor) instructorBox.getSelectedItem();

        if (selectedDate == null || selectedInstructor == null) {
            return;
        }

        int instructorId = selectedInstructor.getId();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT start_time FROM availability WHERE instructor_id = ? AND date = ? AND is_booked = 0";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, instructorId);
            stmt.setString(2, dateStr);
            ResultSet rs = stmt.executeQuery();



            while (rs.next()) {
                String startTimeStr = rs.getString("start_time");
                startTimeComboBox.addItem(startTimeStr);
            }

            if (startTimeComboBox.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "Seçilen tarih için uygun saat bulunamadı.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Saatler yüklenirken bir hata oluştu.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}

        






