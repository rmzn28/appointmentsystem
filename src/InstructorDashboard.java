import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.toedter.calendar.JDateChooser;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class InstructorDashboard extends JFrame {
    private final int instructorId;
    private final JPanel requestPanel;
    private final JDateChooser dateChooser;
    private final JSpinner startTimeSpinner;
    private final JSpinner endTimeSpinner;

    private final JPanel contentPanel;
    private final CardLayout cardLayout;

    public InstructorDashboard(int instructorId) {
        this.instructorId = instructorId;
        setTitle("Öğretim Üyesi Paneli");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Üst panel: Butonlar
        JPanel topPanel = new JPanel();
        JButton viewRequestsBtn = new JButton("Randevu İstekleri");
        viewRequestsBtn.addActionListener(e -> showRequestsPanel());

        JButton addAvailabilityBtn = new JButton("Müsaitlik Ekle");
        addAvailabilityBtn.addActionListener(e -> showAvailabilityForm());

        topPanel.add(viewRequestsBtn);
        topPanel.add(addAvailabilityBtn);
        add(topPanel, BorderLayout.NORTH);

        // CardLayout ve paneller
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Randevu istekleri paneli
        requestPanel = new JPanel();
        requestPanel.setLayout(new BoxLayout(requestPanel, BoxLayout.Y_AXIS));
        JScrollPane requestScrollPane = new JScrollPane(requestPanel);
        requestScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        contentPanel.add(requestScrollPane, "requests");

        // Müsaitlik ekleme paneli
        JPanel availabilityPanel = new JPanel(new GridLayout(4, 2));

        dateChooser = new JDateChooser();

        SpinnerDateModel startModel = new SpinnerDateModel();
        startTimeSpinner = new JSpinner(startModel);
        startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, "HH:mm"));

        SpinnerDateModel endModel = new SpinnerDateModel();
        endTimeSpinner = new JSpinner(endModel);
        endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "HH:mm"));

        JButton addAvailabilitySubmitBtn = new JButton("Müsaitlik Ekle");
        addAvailabilitySubmitBtn.addActionListener(e -> addAvailability());

        availabilityPanel.add(new JLabel("Tarih:"));
        availabilityPanel.add(dateChooser);
        availabilityPanel.add(new JLabel("Başlangıç Saati:"));
        availabilityPanel.add(startTimeSpinner);
        availabilityPanel.add(new JLabel("Bitiş Saati:"));
        availabilityPanel.add(endTimeSpinner);
        availabilityPanel.add(new JLabel());
        availabilityPanel.add(addAvailabilitySubmitBtn);

        contentPanel.add(availabilityPanel, "availability");

        add(contentPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void showRequestsPanel() {
        loadAppointmentRequests();
        cardLayout.show(contentPanel, "requests");
    }

    private void showAvailabilityForm() {
        cardLayout.show(contentPanel, "availability");
    }

    private void loadAppointmentRequests() {
        requestPanel.removeAll();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT a.id, u.name, a.date, a.start_time, a.end_time, a.status " +
                    "FROM appointments a " +
                    "JOIN users u ON a.student_id = u.id " +
                    "WHERE a.instructor_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, instructorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String student = rs.getString("name");
                String date = rs.getString("date");
                String startTime = rs.getString("start_time");
                String endTime = rs.getString("end_time");
                String status = rs.getString("status");

                JPanel appointmentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                appointmentPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

                JLabel studentLabel = new JLabel("Öğrenci: " + student);
                JLabel timeLabel = new JLabel("Saat: " + date + " " + startTime + " - " + endTime);
                JLabel statusLabel = new JLabel("Durum: " + status);

                appointmentPanel.add(studentLabel);
                appointmentPanel.add(timeLabel);
                appointmentPanel.add(statusLabel);

                if (status.equalsIgnoreCase("Beklemede")) {
                    JButton approveButton = new JButton("Onayla");
                    JButton rejectButton = new JButton("Reddet");
                    JButton suggestNewTimeButton = new JButton("Yeni Saat Öner");

                    approveButton.addActionListener(e -> {
                        updateStatusAndReload(id, "Onaylandı");
                    });
                    rejectButton.addActionListener(e -> {
                        updateStatusAndReload(id, "Reddedildi");
                    });
                    suggestNewTimeButton.addActionListener(e -> {
                        showNewTimeSuggestionForm(id, instructorId);
                    });

                    appointmentPanel.add(approveButton);
                    appointmentPanel.add(rejectButton);
                    appointmentPanel.add(suggestNewTimeButton);
                }

                requestPanel.add(appointmentPanel);
            }



            requestPanel.revalidate();// Yapısal güncelleme (layout).
            requestPanel.repaint();   // Görsel güncelleme (ekrana yansıtma).

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Durum güncelle ve paneli yeniden yükle
    private void updateStatusAndReload(int appointmentId, String newStatus) {
        try (Connection conn = DBConnection.getConnection()) {
            // 1- Randevu bilgilerini çek (date, start_time, end_time, instructor_id)
            String selectSql = "SELECT date, start_time, end_time, instructor_id FROM appointments WHERE id = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setInt(1, appointmentId);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                String date = rs.getString("date");
                String startTime = rs.getString("start_time");
                String endTime = rs.getString("end_time");
                int instructorIdFromDb = rs.getInt("instructor_id");

                // 2- Onay durumuysa availability tablosundaki isbooked değerini güncelle
                if (newStatus.equalsIgnoreCase("Onaylandı")) {
                    String updateAvailabilitySql = "UPDATE availability SET is_booked = 1 WHERE instructor_id = ? AND date = ? AND start_time = ? AND end_time = ?";
                    PreparedStatement updateAvailabilityStmt = conn.prepareStatement(updateAvailabilitySql);
                    updateAvailabilityStmt.setInt(1, instructorIdFromDb);
                    updateAvailabilityStmt.setString(2, date);
                    updateAvailabilityStmt.setString(3, startTime);
                    updateAvailabilityStmt.setString(4, endTime);
                    updateAvailabilityStmt.executeUpdate();
                }

                // 3- appointments tablosundaki status güncelle
                String updateAppointmentSql = "UPDATE appointments SET status = ? WHERE id = ?";
                PreparedStatement updateAppointmentStmt = conn.prepareStatement(updateAppointmentSql);
                updateAppointmentStmt.setString(1, newStatus);
                updateAppointmentStmt.setInt(2, appointmentId);
                int updated = updateAppointmentStmt.executeUpdate();

                if (updated > 0) {
                    JOptionPane.showMessageDialog(this, "Durum güncellendi: " + newStatus);
                    loadAppointmentRequests(); // Paneli yenile
                } else {
                    JOptionPane.showMessageDialog(this, "Durum güncellenemedi.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Randevu bulunamadı.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Veritabanı hatası oluştu.");
        }
    }



    private void showNewTimeSuggestionForm(int appointmentId, int instructorId) {
        JPanel suggestionPanel = new JPanel(new GridLayout(3, 2));

        JDateChooser newDateChooser = new JDateChooser();
        suggestionPanel.add(new JLabel("Yeni Tarih:"));
        suggestionPanel.add(newDateChooser);

        suggestionPanel.add(new JLabel("Yeni Başlangıç Saati:"));
        JComboBox<String> startTimeComboBox = new JComboBox<>();
        suggestionPanel.add(startTimeComboBox);


        // Yeni tarih seçildiğinde veya form açıldığında availability'den müsait saatleri getirip combobox'a ekle
        newDateChooser.addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                Date selectedDate = newDateChooser.getDate();
                if (selectedDate != null) {
                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
                    startTimeComboBox.removeAllItems();

                    try (Connection conn = DBConnection.getConnection()) {
                        String sql = "SELECT start_time FROM availability WHERE instructor_id = ? AND date = ? AND is_booked = 0 ORDER BY start_time";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, instructorId);
                        stmt.setString(2, formattedDate);
                        ResultSet rs = stmt.executeQuery();

                        while (rs.next()) {
                            String startTime = rs.getString("start_time");
                            startTimeComboBox.addItem(startTime);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Müsait saatler yüklenirken hata oluştu.");
                    }
                }
            }
        });



        int option = JOptionPane.showConfirmDialog(this, suggestionPanel, "Yeni Saat Öner", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            Date newDate = newDateChooser.getDate();
            if (newDate == null) {
                JOptionPane.showMessageDialog(this, "Lütfen bir tarih seçin.");
                return;
            }

            String newStartTime = (String) startTimeComboBox.getSelectedItem();
            if (newStartTime == null) {
                JOptionPane.showMessageDialog(this, "Lütfen bir başlangıç saati seçin.");
                return;
            }



            String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(newDate);

            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);

                // 1. appointments tablosunu güncelle
                String updateAppointmentSql = "UPDATE appointments SET proposed_date = ?, proposed_time = ?, status = ? WHERE id = ?";
                PreparedStatement updateAppointmentStmt = conn.prepareStatement(updateAppointmentSql);
                updateAppointmentStmt.setString(1, formattedDate);
                updateAppointmentStmt.setString(2, newStartTime);
                updateAppointmentStmt.setString(3, "Yeni Saat Önerildi");
                updateAppointmentStmt.setInt(4, appointmentId);
                int rowsUpdated = updateAppointmentStmt.executeUpdate();

                if (rowsUpdated > 0) {
                    // 2. availability tablosunda o saat için isbooked=1 yap
                    String updateAvailabilitySql = "UPDATE availability SET is_booked = 1 WHERE instructor_id = ? AND date = ? AND start_time = ?";
                    PreparedStatement updateAvailabilityStmt = conn.prepareStatement(updateAvailabilitySql);
                    updateAvailabilityStmt.setInt(1, instructorId);
                    updateAvailabilityStmt.setString(2, formattedDate);
                    updateAvailabilityStmt.setString(3, newStartTime);
                    int availUpdated = updateAvailabilityStmt.executeUpdate();

                    if (availUpdated > 0) {
                        conn.commit();
                        JOptionPane.showMessageDialog(this, "Yeni saat önerildi: " + formattedDate + " " + newStartTime);
                        loadAppointmentRequests();
                    } else {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "Müsaitlik tablosu güncellenemedi.");
                    }
                } else {
                    conn.rollback(); // Başarısızsa geri al
                    JOptionPane.showMessageDialog(this, "Saat önerisi kaydedilemedi.");
                }
                conn.setAutoCommit(true);// Her şey başarılıysa işlemleri kalıcı yap
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Veritabanı hatası oluştu.");
            }
        }
    }

    private void addAvailability() {
        Date selectedDate = dateChooser.getDate();
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Lütfen bir tarih seçin.");
            return;
        }

        // Spinner'lardan saat bilgisi alınıyor
        Date startTime = (Date) startTimeSpinner.getValue();
        Date endTime = (Date) endTimeSpinner.getValue();

        // Tarih ve saat bilgilerini birleştirmek için Calendar kullanıyoruz
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(selectedDate);
        Calendar tempCal = Calendar.getInstance();

        // startTime'ın saat ve dakika bilgisini alıp calendarStart'a set ediyoruz
        tempCal.setTime(startTime);
        calendarStart.set(Calendar.HOUR_OF_DAY, tempCal.get(Calendar.HOUR_OF_DAY));
        calendarStart.set(Calendar.MINUTE, tempCal.get(Calendar.MINUTE));
        calendarStart.set(Calendar.SECOND, 0);
        calendarStart.set(Calendar.MILLISECOND, 0);
        Date startDateTime = calendarStart.getTime();

        // Aynı işlemi endTime için yapıyoruz
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(selectedDate);
        tempCal.setTime(endTime);
        calendarEnd.set(Calendar.HOUR_OF_DAY, tempCal.get(Calendar.HOUR_OF_DAY));
        calendarEnd.set(Calendar.MINUTE, tempCal.get(Calendar.MINUTE));
        calendarEnd.set(Calendar.SECOND, 0);
        calendarEnd.set(Calendar.MILLISECOND, 0);
        Date endDateTime = calendarEnd.getTime();

        // Başlangıç saati bitiş saatinden önce olmalı
        if (!startDateTime.before(endDateTime)) {
            JOptionPane.showMessageDialog(this, "Başlangıç saati bitiş saatinden önce olmalıdır.");
            return;
        }

        long interval = 20 * 60 * 1000; // 20 dakika milisaniye cinsinden
        long startMillis = startDateTime.getTime();
        long endMillis = endDateTime.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        String formattedDate = dateFormat.format(selectedDate);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO availability (instructor_id, date, start_time, end_time, is_booked) VALUES (?, ?, ?, ?, 0)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            while (startMillis + interval <= endMillis) {
                Date slotStart = new Date(startMillis);
                Date slotEnd = new Date(startMillis + interval);

                stmt.setInt(1, instructorId);
                stmt.setString(2, formattedDate);
                stmt.setString(3, timeFormat.format(slotStart));
                stmt.setString(4, timeFormat.format(slotEnd));
                stmt.addBatch();

                startMillis += interval;
            }

            stmt.executeBatch();

            JOptionPane.showMessageDialog(this, "Müsaitlikler 20 dakikalık aralıklarla başarıyla eklendi.");

            // Arayüz temizliği
            dateChooser.setDate(null);
            startTimeSpinner.setValue(new Date());
            endTimeSpinner.setValue(new Date());

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Veritabanı hatası oluştu.");
        }
    }



}


