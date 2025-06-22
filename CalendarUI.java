// === File: CalendarUI.java ===

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;

class CalendarUI extends JFrame {
    private final JPanel calendarPanel;
    private final JLabel monthLabel;
    private final Calendar calendar = Calendar.getInstance();
    private final Map<String, java.util.List<String>> taskMap = new HashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");

    public CalendarUI(String username) {
        setTitle("TeamTasker Calendar - " + username);
        setSize(920, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 255));

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        headerPanel.setBackground(new Color(0x2575fc));

        JButton prevMonth = new JButton("◀");
        JButton nextMonth = new JButton("▶");
        styleHeaderButton(prevMonth);
        styleHeaderButton(nextMonth);

        monthLabel = new JLabel();
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        monthLabel.setForeground(Color.WHITE);
        updateMonthLabel();

        prevMonth.addActionListener(e -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        nextMonth.addActionListener(e -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        headerPanel.add(prevMonth);
        headerPanel.add(monthLabel);
        headerPanel.add(nextMonth);

        // Calendar grid
        calendarPanel = new JPanel(new GridLayout(0, 7));
        calendarPanel.setBackground(Color.WHITE);
        updateCalendar();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(calendarPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
        setVisible(true);

        // Start the reminder thread
        new ReminderThread(taskMap).start();
    }

    private void updateMonthLabel() {
        monthLabel.setText(dateFormat.format(calendar.getTime()));
    }

    private void updateCalendar() {
        calendarPanel.removeAll();
        updateMonthLabel();

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            JLabel lbl = new JLabel(day, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lbl.setForeground(new Color(0x2575fc));
            calendarPanel.add(lbl);
        }

        Calendar cal = (Calendar) calendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int startDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < startDay; i++) calendarPanel.add(new JLabel(""));

        for (int day = 1; day <= maxDay; day++) {
            Calendar dateCal = (Calendar) calendar.clone();
            dateCal.set(Calendar.DAY_OF_MONTH, day);
            String key = new SimpleDateFormat("yyyy-MM-dd").format(dateCal.getTime());

            JButton dayBtn = new JButton("<html><center>" + day +
                    (taskMap.containsKey(key) ? "<br>📌" + taskMap.get(key).size() : "") +
                    "</center></html>");
            dayBtn.setBackground(new Color(230, 240, 255));
            dayBtn.setFocusPainted(false);
            dayBtn.setBorder(new LineBorder(new Color(0x2575fc), 1));
            dayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            int finalDay = day;
            dayBtn.addActionListener(e -> openTaskDialog(finalDay));
            calendarPanel.add(dayBtn);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private void openTaskDialog(int day) {
        String key = getSelectedDate(day);
        java.util.List<String> tasks = taskMap.getOrDefault(key, new ArrayList<>());

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        DefaultListModel<String> model = new DefaultListModel<>();
        tasks.forEach(model::addElement);
        JList<String> taskList = new JList<>(model);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(taskList);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn = new JButton("Delete");
        controls.add(addBtn);
        controls.add(editBtn);
        controls.add(delBtn);
        panel.add(controls, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(this, "Tasks on " + key, true);
        dialog.setContentPane(panel);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        addBtn.addActionListener(_e -> {
            String task = promptForTask(null);
            if (task != null) {
                model.addElement(task);
                tasks.add(task);
                taskMap.put(key, tasks);
                updateCalendar();
            }
        });

        editBtn.addActionListener(_e -> {
            int idx = taskList.getSelectedIndex();
            if (idx >= 0) {
                String updated = promptForTask(tasks.get(idx));
                if (updated != null) {
                    tasks.set(idx, updated);
                    model.set(idx, updated);
                    updateCalendar();
                }
            }
        });

        delBtn.addActionListener(_e -> {
            int idx = taskList.getSelectedIndex();
            if (idx >= 0) {
                int confirm = JOptionPane.showConfirmDialog(dialog, "Delete selected task?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    tasks.remove(idx);
                    model.remove(idx);
                    if (tasks.isEmpty()) taskMap.remove(key);
                    updateCalendar();
                }
            }
        });

        dialog.setVisible(true);
    }

    private String promptForTask(String existing) {
        JTextField title = new JTextField();
        JTextField desc = new JTextField();
        JComboBox<String> start = createTimePicker();
        JComboBox<String> end = createTimePicker();

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Title:"));
        panel.add(title);
        panel.add(new JLabel("Description:"));
        panel.add(desc);
        panel.add(new JLabel("Start Time:"));
        panel.add(start);
        panel.add(new JLabel("End Time:"));
        panel.add(end);

        int result = JOptionPane.showConfirmDialog(this, panel, "Task Info", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            return String.format("📌 %s (%s-%s): %s", title.getText(), start.getSelectedItem(), end.getSelectedItem(), desc.getText());
        }
        return null;
    }

    private String getSelectedDate(int day) {
        Calendar cal = (Calendar) calendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, day);
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }

    private JComboBox<String> createTimePicker() {
        String[] times = new String[48];
        for (int i = 0; i < 48; i++) {
            int hour = i / 2;
            int minute = (i % 2) * 30;
            times[i] = String.format("%02d:%02d", hour, minute);
        }
        return new JComboBox<>(times);
    }

    private void styleHeaderButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0x6a11cb));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setBorder(new EmptyBorder(4, 10, 4, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // === ReminderThread (Background thread for task reminders) ===
    private class ReminderThread extends Thread {
        private final Map<String, java.util.List<String>> taskMap;

        public ReminderThread(Map<String, java.util.List<String>> taskMap) {
            this.taskMap = taskMap;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(60_000); // check every minute
                    String now = new SimpleDateFormat("HH:mm").format(new Date());
                    String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                    List<String> todayTasks = taskMap.getOrDefault(today, Collections.emptyList());
                    for (String task : todayTasks) {
                        if (task.contains(now)) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(null,
                                        "⏰ Reminder!\n" + task,
                                        "Task Reminder",
                                        JOptionPane.INFORMATION_MESSAGE);
                            });
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
