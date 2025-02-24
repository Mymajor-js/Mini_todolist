import javax.swing.*;

import javafx.concurrent.Task;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ToDoListGUI extends JFrame {
    private static final String FILE_NAME = "todolist.txt";
    private JTextField titleField, detailField, dateField, timeField;
    private JButton addButton, editButton, successButton, listButton ,refreshButton;
    private JTextArea displayArea;
    private JComboBox<String> statusComboBox ,priorityField,priorityField2;
    private JList<Task> taskList;
    private DefaultListModel<Task> listModel;
    private List<Task> tasks = new ArrayList<>();
    DefaultListModel<Task> listModela = new DefaultListModel<>();
    
    public ToDoListGUI() {
        setTitle("To-Do List");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        taskList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Task selectedTask = taskList.getSelectedValue();
                if (selectedTask != null) {
                    showTaskDetails(selectedTask);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(taskList);
        this.add(scrollPane, BorderLayout.CENTER);
        
        // Input fields panel
        JPanel inputPanel = new JPanel(new GridLayout(7, 1));
        inputPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        inputPanel.add(titleField);

        inputPanel.add(new JLabel("Detail:"));
        detailField = new JTextField();
        inputPanel.add(detailField);

        inputPanel.add(new JLabel("Date (dd/MM/yyyy):"));
        dateField = new JTextField();
        inputPanel.add(dateField);

        inputPanel.add(new JLabel("Time (HH:MM):"));
        timeField = new JTextField();
        inputPanel.add(timeField);

        inputPanel.add(new JLabel("Priority:"));
        priorityField = new JComboBox<>(new String[]{"Important", "Not important"});
        inputPanel.add(priorityField);

        inputPanel.add(new JLabel("Priority:"));
        priorityField2 = new JComboBox<>(new String[]{"Urgent", "Not urgent"});
        inputPanel.add(priorityField2);

        //inputPanel.add(new JLabel("Status:"));
        //statusComboBox = new JComboBox<>(new String[]{"Incomplete", "Complete"});
        //inputPanel.add(statusComboBox);

        add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add Task");
        buttonPanel.add(addButton);
        editButton = new JButton("Open Task");
        buttonPanel.add(editButton);
        successButton = new JButton("Success (Complete)");
        buttonPanel.add(successButton);
        listButton = new JButton("Delete Task");
        buttonPanel.add(listButton);
        refreshButton = new JButton("Refresh");
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        JScrollPane displayScroll = new JScrollPane(displayArea);
        add(displayScroll, BorderLayout.EAST);

        addButton.addActionListener(e -> {
            reloadTasksFromFile();
            addTask();});
        editButton.addActionListener(e -> {
            Task selectedTask = taskList.getSelectedValue();
            if (selectedTask != null) {
                showTaskInNewWindow(selectedTask);
            }
        });
        taskList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Task selectedTask = taskList.getSelectedValue();
                if (selectedTask != null) {
                    showTaskDetails(selectedTask);
                }
            }
        });
        listButton.addActionListener(e -> deleteTask());
        loadTasksFromFile();
        refreshButton.addActionListener(e -> {
            toString();
            refreshTaskList();
            reloadTasksFromFile();});
            successButton.addActionListener(e -> {
                Task selectedTask = taskList.getSelectedValue();
                if (selectedTask != null) {
                    completeSave(selectedTask);
                    reloadTasksFromFile();
                }
            });
            
    }
    private void completeSave(Task selectedTask) {
        try {
            List<String> lines = Files.readAllLines(Paths.get("todolist.txt"));
            StringBuilder updatedContent = new StringBuilder();
            boolean isTaskFound = false;
    
            for (String line : lines) {
                if (line.contains("<title> " + selectedTask.getTitle())) {
                    isTaskFound = true;
                }
                if (isTaskFound && line.startsWith("<status>")) {
                    line = "<status> Complete";
                    isTaskFound = false; 
                }
                updatedContent.append(line).append(System.lineSeparator());
            }
            Files.write(Paths.get("todolist.txt"), updatedContent.toString().getBytes());
            System.out.println("Task status updated successfully.");
            refreshTaskList();
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void saveTasksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Task task : tasks) {
                writer.write("<no> " + getNextNumber() + "\n");
                writer.write("<title> " + task.title + "\n");
                writer.write("<detail> " + task.detail + "\n");
                writer.write("<date> " + task.date + "\n");
                writer.write("<time> " + task.time + "\n");
                writer.write("<priority> " + task.priority + " and " + task.priority2 +"\n");
                writer.write("<status> " + "Incomplete" + "\n");
                writer.write("</no>\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadTasksFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            Task currentTask = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("<no>")) {
                    if (currentTask != null) {
                        tasks.add(currentTask);
                        listModel.addElement(currentTask);
                    }
                    currentTask = new Task("", "", "", "", "", "","");
                }
                if (line.startsWith("<title>")) {
                    if (currentTask != null) {
                        currentTask.setTitle(line.replace("<title>", "").trim());
                    }
                } else if (line.startsWith("<detail>")) {
                    if (currentTask != null) {
                        currentTask.setDetail(line.replace("<detail>", "").trim());
                    }
                } else if (line.startsWith("<date>")) {
                    if (currentTask != null) {
                        currentTask.setDate(line.replace("<date>", "").trim());
                    }
                } else if (line.startsWith("<time>")) {
                    if (currentTask != null) {
                        currentTask.setTime(line.replace("<time>", "").trim());
                    }
                } else if (line.startsWith("<priority>")) {
                    if (currentTask != null) {
                        currentTask.setPriority(line.replace("<priority>", "").trim());
                    }
                } else if (line.startsWith("<status>")) {
                    if (currentTask != null) {
                        currentTask.setStatus(line.replace("<status>", "").trim());
                    }
                }
                if (line.startsWith("</no>")) {
                    if (currentTask != null) {
                        tasks.add(currentTask);
                        listModel.addElement(currentTask);
                    }
                    currentTask = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshTaskList();

    }
    private void reloadTasksFromFile() {
        tasks.clear(); 
        listModel.clear();
    
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            Task currentTask = null;
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("<no>")) {
                    if (currentTask != null) {
                        tasks.add(currentTask);
                        listModel.addElement(currentTask);
                    }
                    currentTask = new Task("", "", "", "", "", "", "");
                }
                if (currentTask != null) {
                    if (line.startsWith("<title>")) {
                        currentTask.setTitle(line.replace("<title>", "").trim());
                    } else if (line.startsWith("<detail>")) {
                        currentTask.setDetail(line.replace("<detail>", "").trim());
                    } else if (line.startsWith("<date>")) {
                        currentTask.setDate(line.replace("<date>", "").trim());
                    } else if (line.startsWith("<time>")) {
                        currentTask.setTime(line.replace("<time>", "").trim());
                    } else if (line.startsWith("<priority>")) {
                        currentTask.setPriority(line.replace("<priority>", "").trim());
                    } else if (line.startsWith("<status>")) {
                        currentTask.setStatus(line.replace("<status>", "").trim());
                    }
                }
                if (line.startsWith("</no>") && currentTask != null) {
                    tasks.add(currentTask);
                    listModel.addElement(currentTask);
                    currentTask = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        taskList.setModel(listModel);
    }
    

    private boolean isValidDate(String dateField) {
    String pattern = "\\d{2}/\\d{2}/\\d{4}"; 
    if (!dateField.matches(pattern)) {
        JOptionPane.showMessageDialog(null, "Format of data (dd/MM/yyyy)", "Format error Date", JOptionPane.ERROR_MESSAGE);
        return false;
    }
    
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    sdf.setLenient(false);
    try {
        sdf.parse(dateField);
        return true;
    } catch (ParseException e) {
        JOptionPane.showMessageDialog(null, "Format error Date", "error", JOptionPane.ERROR_MESSAGE);
        return false;
    }
}
private boolean isValidTime(String timeField) {
    String pattern = "\\d{2}:\\d{2}";
    if (!timeField.matches(pattern)) {
        JOptionPane.showMessageDialog(null, "Format of Time (HH:mm)", "Format error HH:mm", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    try {
        int hour = Integer.parseInt(timeField.substring(0, 2));
        int minute = Integer.parseInt(timeField.substring(3, 5));
        if (hour >= 0 && hour < 24 && minute >= 0 && minute < 60) {
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "Format time 00:00 - 23:59", "error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "!Format of Error (HH:mm)", "error", JOptionPane.ERROR_MESSAGE);
        return false;
    }
}


    private void addTask() {
        
        String title = titleField.getText();
        String detail = detailField.getText();
        String date = dateField.getText();
        String time = timeField.getText();
        String priority = (String) priorityField.getSelectedItem(); 
        
        String priority2 = (priorityField2 != null && priorityField2.getSelectedItem() != null)
                ? (String) priorityField2.getSelectedItem()
                : "Not Set"; 
        String status = "Incomplete";
    
        if (title.isEmpty() || detail.isEmpty() || date.isEmpty() || time.isEmpty() || priority == null || priority2 == null || status == null) {
            JOptionPane.showMessageDialog(this, "data mai croup");
            return;
        }
        if (!isValidTime(time) || !isValidDate(date)) {
            JOptionPane.showMessageDialog(this, "Incorrect information ", "error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Task newTask = new Task(title, detail, date, time, priority, status, priority2);
        tasks.add(newTask);
    
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write("<no> " + getNextNumber() + "\n");
            writer.write("<title> " + title + "\n");
            writer.write("<detail> " + detail + "\n");
            writer.write("<date> " + date + "\n");
            writer.write("<time> " + time + "\n");
            writer.write("<priority> " + priority + " " + priority2 + "\n");
            writer.write("<status> "+ status +  "\n");
            writer.write("</no>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        toString();
        refreshTaskList();
        reloadTasksFromFile();
    }
    
    private void refreshTaskList() {
        if (tasks.isEmpty()) {
            displayArea.setText("ไม่มีงานในรายการ");
            return;
        }
    
        Map<String, List<Task>> tasksByDate = new TreeMap<>();
    
        for (Task task : tasks) {
            String date = task.getDate().trim();
            if (!date.isEmpty()) {
                tasksByDate.putIfAbsent(date, new ArrayList<>());
                tasksByDate.get(date).add(task);
            }
        }
    
        StringBuilder displayText = new StringBuilder();
    
        displayText.append("================={ My Work }=================\n");
    
        for (Map.Entry<String, List<Task>> entry : tasksByDate.entrySet()) {
            String date = entry.getKey();
            displayText.append(date).append("\n");
    
            for (Task task : entry.getValue()) {
                displayText.append("  - ")
                           .append(task.getTitle()).append(" | ")
                           .append(task.getTime()).append(" | ")
                           .append(task.getStatus())
                           .append("\n");
            }
        }
    
        displayArea.setText(displayText.toString().trim());
        displayArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int position = displayArea.viewToModel(e.getPoint());
                String text = displayArea.getText();
                int start = text.lastIndexOf("\n", position) + 1;
                int end = text.indexOf("\n", position);
                if (end == -1) end = text.length();
                String selectedTaskInfo = text.substring(start, end).trim();
    
                if (selectedTaskInfo.startsWith("  - ")) {
                    showTaskDetailsForSelection(selectedTaskInfo);
                }
            }
        });
    }
    
    private void showTaskDetailsForSelection(String selectedTaskInfo) {
        for (Task task : tasks) {
            String taskSummary = "  - " + task.getTitle() + " | " + task.getTime() + " | " + task.getStatus();
            if (selectedTaskInfo.equals(taskSummary)) {
                titleField.setText(task.getTitle());
                detailField.setText(task.getDetail());
                dateField.setText(task.getDate());
                timeField.setText(task.getTime());
                priorityField.setSelectedItem(task.getStatus());
                priorityField2.setSelectedItem(task.getStatus());
                statusComboBox.setSelectedItem(task.getStatus());
                break;
            }
        }
    }
    
    private void deleteTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            tasks.remove(selectedIndex);
            listModel.remove(selectedIndex);
            saveTasksToFile();
            refreshTaskList();
        }
    }

    private static int getNextNumber() {
        int maxNumber = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("<no>")) {
                    String numberStr = line.replace("<no>", "").trim();
                    int number = Integer.parseInt(numberStr);
                    maxNumber = Math.max(maxNumber, number);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return maxNumber + 1;
    }
private void showTaskInNewWindow(Task task) {
    JFrame taskDetailsWindow = new JFrame("Task Details");
    taskDetailsWindow.setSize(400, 300);
    taskDetailsWindow.setLocationRelativeTo(this);
    taskDetailsWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(6, 2));
    
    panel.add(new JLabel("Title:"));
    panel.add(new JLabel(task.getTitle()));
    
    panel.add(new JLabel("Detail:"));
    panel.add(new JLabel(task.getDetail()));
    
    panel.add(new JLabel("Date:"));
    panel.add(new JLabel(task.getDate()));
    
    panel.add(new JLabel("Time:"));
    panel.add(new JLabel(task.getTime()));
    
    String priorityText = task.getPriority() + " " + task.getPriority2();

    panel.add(new JLabel("Priority:"));

    JLabel priorityLabel = new JLabel(priorityText);

    panel.add(priorityLabel);

    
    panel.add(new JLabel("Status:"));
    panel.add(new JLabel(task.getStatus()));
    
    taskDetailsWindow.add(panel);
    taskDetailsWindow.setVisible(true);
}


    private void showTaskDetails(Task task) {
        displayArea.setText(task.getTaskDetails());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ToDoListGUI().setVisible(true));
    }

    // Class to represent a Task
    private class Task {
        private String title;
        private String detail;
        private String date;
        private String time;
        private String priority;
        private String priority2;
        private String status;

        public Task(String title, String detail, String date, String time, String priority, String status, String priority2) {
            this.title = title;
            this.detail = detail;
            this.date = date;
            this.time = time;
            this.priority = priority;
            this.priority2 = priority2;
            this.status = status;
        }

        public String getTitle() {
            return title;
        }

        public String getDetail() {
            return detail;
        }
        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getPriority() {
            return priority;
        }
        public String getPriority2() {
            return priority2;
        }
        public String getStatus() {
            return status;
        }
        public void setTitle(String title) {
            this.title = title;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }
        public void setPriority2(String priority2) {
            this.priority2 = priority2;
        }
        public void setStatus(String status) {
            this.status = status;
        }
        public String getTaskDetails() {
            return "title : " + title + "\ndetail : " + detail + "\ndate : " + date + "\ntime : " + time 
                    + "\npriority : " + priority + "\nstatus : " + status;
        }

        // ในคลาส Task
        @Override
public String toString() {
    String statusLabel = status;

    if (status.equals("Complete")) {
        statusLabel = "[Complete] ";
    } else if (status.equals("Incomplete")) {
        statusLabel = "[Incomplete] ";
    }

    if (title.isEmpty() && detail.isEmpty() && time.isEmpty() && status.isEmpty()) {
        return date;  // แสดงแค่วันที่
    }
    refreshTaskList();
    return "  - " + statusLabel + title ;
}

    }
}