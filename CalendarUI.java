import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class CalendarUI extends JFrame {
    private final JPanel calendarPanel;
    private final JLabel monthLabel;
    private final JLabel clockLabel = new JLabel();
    private final Calendar calendar = Calendar.getInstance();
    private final Map<String, List<Task>> taskMap = new HashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");

    public CalendarUI(String username) {
        setTitle("TeamTasker Calendar - " + username);
        setSize(920, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 255));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        headerPanel.setBackground(new Color(0x2575fc));

        JPanel centerHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        centerHeader.setBackground(new Color(0x2575fc));

        JButton prevMonth = new JButton("<");
        JButton nextMonth = new JButton(">");
        styleHeaderArrow(prevMonth);
        styleHeaderArrow(nextMonth);

        monthLabel = new JLabel();
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        monthLabel.setForeground(Color.WHITE);
        updateMonthLabel();

        centerHeader.add(prevMonth);
        centerHeader.add(monthLabel);
        centerHeader.add(nextMonth);

        clockLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        clockLabel.setForeground(Color.WHITE);
        JPanel clockPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        clockPanel.setBackground(new Color(0x2575fc));
        clockPanel.add(clockLabel);

        headerPanel.add(centerHeader, BorderLayout.CENTER);
        headerPanel.add(clockPanel, BorderLayout.EAST);

        prevMonth.addActionListener(e -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        nextMonth.addActionListener(e -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        calendarPanel = new JPanel(new GridLayout(0, 7));
        calendarPanel.setBackground(Color.WHITE);
        updateCalendar();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(calendarPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
        setVisible(true);

        updateClockLabel();
        startClockThread();
        new ReminderThread(taskMap).start();
    }

    private void updateClockLabel() {
        clockLabel.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }

    private void startClockThread() {
        new javax.swing.Timer(1000, e -> {
            updateClockLabel();
            updateCalendar(); // keep refreshing to show "Overdue"
        }).start();
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

        for (int i = 0; i < startDay; i++) {
            calendarPanel.add(new JLabel(""));
        }

        for (int day = 1; day <= maxDay; day++) {
            Calendar dateCal = (Calendar) calendar.clone();
            dateCal.set(Calendar.DAY_OF_MONTH, day);
            String key = new SimpleDateFormat("yyyy-MM-dd").format(dateCal.getTime());

            JPanel dayPanel = new JPanel();
            dayPanel.setLayout(new BoxLayout(dayPanel, BoxLayout.Y_AXIS));
            dayPanel.setBackground(new Color(230, 240, 255));
            dayPanel.setBorder(new LineBorder(new Color(0x2575fc), 1));
            dayPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel dayLabel = new JLabel(String.valueOf(day));
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            dayLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            dayLabel.setBorder(new EmptyBorder(6, 0, 2, 0));
            dayPanel.add(dayLabel);

            if (taskMap.containsKey(key)) {
                long unfinished = taskMap.get(key).stream().filter(t -> !t.done).count();
                long overdue = taskMap.get(key).stream().filter(t -> !t.done && isPastDue(t.text, key)).count();
                if (unfinished > 0) {
                    JLabel taskCountLabel = new JLabel("\uD83D\uDCCC " + unfinished + " Unfinished");
                    taskCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    taskCountLabel.setForeground(Color.DARK_GRAY);
                    taskCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    taskCountLabel.setBorder(new EmptyBorder(2, 0, 0, 0));
                    dayPanel.add(taskCountLabel);
                }
                if (overdue > 0) {
                    JLabel overdueLabel = new JLabel("\u26A0 " + overdue + " Overdue");
                    overdueLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    overdueLabel.setForeground(Color.RED);
                    overdueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    overdueLabel.setBorder(new EmptyBorder(0, 0, 6, 0));
                    dayPanel.add(overdueLabel);
                }
            }

            int finalDay = day;
            dayPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    openTaskDialog(finalDay);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    dayPanel.setBackground(new Color(210, 225, 250));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    dayPanel.setBackground(new Color(230, 240, 255));
                }
            });

            calendarPanel.add(dayPanel);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private boolean isPastDue(String text, String taskDateKey) {
        try {
            Date taskDate = new SimpleDateFormat("yyyy-MM-dd").parse(taskDateKey);
            Date today = new SimpleDateFormat("yyyy-MM-dd").parse(
                    new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

            if (taskDate.before(today)) {
                return true;
            }

            if (taskDate.equals(today)) {
                int dashIdx = text.indexOf('-');
                int parenIdx = text.indexOf(')', dashIdx);
                if (dashIdx != -1 && parenIdx != -1) {
                    String endTime = text.substring(dashIdx + 1, parenIdx).trim();
                    Date end = new SimpleDateFormat("HH:mm").parse(endTime);
                    String nowTime = new SimpleDateFormat("HH:mm").format(new Date());
                    Date now = new SimpleDateFormat("HH:mm").parse(nowTime);
                    return now.after(end);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void openTaskDialog(int day) {
        String key = getSelectedDate(day);
        List<Task> tasks = taskMap.getOrDefault(key, new ArrayList<>());

        JDialog dialog = new JDialog(this, "Tasks on " + key, true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(taskPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Task List"));

        for (Task task : tasks) {
            JCheckBox checkBox = new JCheckBox(task.text);
            checkBox.setSelected(task.done);
            styleCheckbox(checkBox, task.done);

            checkBox.addActionListener(e -> {
                task.done = checkBox.isSelected();
                styleCheckbox(checkBox, task.done);
                updateCalendar();
            });

            taskPanel.add(checkBox);
        }

        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controls.setBackground(Color.WHITE);

        JButton addBtn = new JButton("Add");
        JButton delBtn = new JButton("Delete Selected");

        addBtn.setBackground(new Color(0x2575fc));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        delBtn.setBackground(new Color(0xB00020));
        delBtn.setForeground(Color.WHITE);
        delBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        controls.add(addBtn);
        controls.add(delBtn);
        dialog.add(controls, BorderLayout.SOUTH);

        addBtn.addActionListener(_e -> {
            String newTask = promptForTask(null);
            if (newTask != null && !newTask.trim().isEmpty()) {
                Task task = new Task(newTask, false);
                tasks.add(task);
                taskMap.put(key, tasks);
                dialog.dispose();
                openTaskDialog(day);
                updateCalendar();
            }
        });

        delBtn.addActionListener(_e -> {
            Component[] comps = taskPanel.getComponents();
            Iterator<Task> iter = tasks.iterator();
            int i = 0;
            while (iter.hasNext()) {
                Task t = iter.next();
                JCheckBox cb = (JCheckBox) comps[i++];
                if (cb.isSelected() && JOptionPane.showConfirmDialog(dialog, "Delete task: \"" + t.text + "\"?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    iter.remove();
                }
            }
            if (tasks.isEmpty()) taskMap.remove(key);
            dialog.dispose();
            openTaskDialog(day);
            updateCalendar();
        });

        dialog.setVisible(true);
    }

    private String getSelectedDate(int day) {
        Calendar cal = (Calendar) calendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, day);
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }

    private void styleCheckbox(JCheckBox cb, boolean isDone) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cb.setForeground(isDone ? Color.GRAY : Color.BLACK);
        cb.setText(isDone ? "<html><strike>" + cb.getText() + "</strike></html>" : cb.getText().replaceAll("<[^>]+>", ""));
    }

    private void styleHeaderArrow(JButton btn) {
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0x2575fc));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private String promptForTask(String existing) {
        JTextField title = new JTextField();
        JTextField desc = new JTextField();
        JFormattedTextField start = new JFormattedTextField(new SimpleDateFormat("HH:mm"));
        JFormattedTextField end = new JFormattedTextField(new SimpleDateFormat("HH:mm"));
        start.setColumns(5);
        end.setColumns(5);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Title:"));
        panel.add(title);
        panel.add(new JLabel("Description:"));
        panel.add(desc);
        panel.add(new JLabel("Start Time (HH:mm):"));
        panel.add(start);
        panel.add(new JLabel("End Time (HH:mm):"));
        panel.add(end);

        int result = JOptionPane.showConfirmDialog(this, panel, "Task Info", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            return String.format("\uD83D\uDCCC %s (%s-%s): %s", title.getText(), start.getText(), end.getText(), desc.getText());
        }
        return null;
    }

    private static class Task {
        String text;
        boolean done;

        Task(String text, boolean done) {
            this.text = text;
            this.done = done;
        }
    }

    private static class ReminderThread extends Thread {
    private final Map<String, List<Task>> taskMap;
    private final Set<String> remindedTasks = new HashSet<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public ReminderThread(Map<String, List<Task>> taskMap) {
        this.taskMap = taskMap;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                String nowTime = new SimpleDateFormat("HH:mm:ss").format(new Date());

                Calendar todayCal = Calendar.getInstance();
                String today = dateFormat.format(todayCal.getTime());

                // 🔔 1. Same-Day Reminders (at task time)
                List<Task> todayTasks = taskMap.getOrDefault(today, Collections.emptyList());
                for (Task task : todayTasks) {
                    if (task.done) continue;
                String key = "TODAY" + today + task.text;
                    if (!remindedTasks.contains(key)) {
                    remindedTasks.add(key);
                String title = extractTaskTitle(task.text);
                    showReminder("🟢 Task Due Today", "Don't forget to do: \"" + title + "\" today.");
            }
        }

                // 📅 2. Tomorrow Reminders (advance notice)
                Calendar tomorrowCal = (Calendar) todayCal.clone();
                tomorrowCal.add(Calendar.DAY_OF_YEAR, 1);
                String tomorrow = dateFormat.format(tomorrowCal.getTime());

                List<Task> tomorrowTasks = taskMap.getOrDefault(tomorrow, Collections.emptyList());
                for (Task task : tomorrowTasks) {
                    String key = "TMR" + tomorrow + task.text;
                    if (!remindedTasks.contains(key)) {
                        remindedTasks.add(key);
                        showReminder("📅 Upcoming Task (Tomorrow)", task.text);
                    }
                }

                // ⚠️ 3. Overdue Yesterday Reminders
                Calendar yesterdayCal = (Calendar) todayCal.clone();
                yesterdayCal.add(Calendar.DAY_OF_YEAR, -1);
                String yesterday = dateFormat.format(yesterdayCal.getTime());

                List<Task> yesterdayTasks = taskMap.getOrDefault(yesterday, Collections.emptyList());
                for (Task task : yesterdayTasks) {
                    if (task.done) continue;
                    String key = "OVD" + yesterday + task.text;
                    if (!remindedTasks.contains(key)) {
                        remindedTasks.add(key);
                        String title = extractTaskTitle(task.text);
                        showReminder("⚠️ Task Overdue!", "This task \"" + title + "\" is overdue (yesterday)");
                    }
                }

            } catch (InterruptedException ignored) {}
        }
    }

    private String extractStartTime(String text) {
        try {
            int startIdx = text.indexOf('(');
            int endIdx = text.indexOf('-', startIdx);
            if (startIdx != -1 && endIdx != -1) {
                return text.substring(startIdx + 1, endIdx).trim();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String extractTaskTitle(String text) {
        try {
            if (text.contains("📌 ")) {
                text = text.substring(text.indexOf("📌 ") + 1);
            } else if (text.contains("📍 ")) {
                text = text.substring(text.indexOf("📍 ") + 2);
            } else if (text.contains("📎 ") || text.contains("📅 ") || text.contains("📋 ")) {
                text = text.substring(text.indexOf(" ") + 1);
            }
            int start = text.indexOf("📌 ") != -1 ? text.indexOf("📌 ") + 2 : text.indexOf("📅 ") + 2;
            int paren = text.indexOf("(", start);
            if (paren != -1) {
                return text.substring(2, paren).trim();
            }
        } catch (Exception ignored) {}
        try {
            int pinInx = text.indexOf("📌");
            int openParen = text.indexOf("(", pinInx);
            if (pinInx != -1 && openParen != -1) {
                return text.substring(pinInx + 2, openParen).trim();
            }
        } catch (Exception ignored) {}
        return text;
    }

    private void showReminder(String title, String message) {
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE)
        );
    }
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CalendarUI("admin"));
    }
}
