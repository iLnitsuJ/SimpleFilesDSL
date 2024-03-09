package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import evaluator.Evaluator;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import parser.*;
import staticCheck.StaticCheck;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import errors.SimpleFilesErrorListener;
import libs.Node;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import parser.ParseToASTVisitor;
import parser.SimpleFilesLexer;

import parser.SimpleFilesParser;
import staticCheck.StaticCheck;


public class UI {
    private JFrame frame;
    private JTextArea inputScriptArea;
    private JTextArea resultArea;
    private JButton executeButton;

    private JButton createFolderButton;
    private JButton createFileButton;
    private JButton groupButton;
    private JButton renameButton;

    public UI() {
        frame = new JFrame("User Interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);

        Dimension buttonSize = new Dimension(50, 30);
        // Initialize buttons
        createFolderButton = new JButton("Insert Create Folder Template");
        createFolderButton.setMaximumSize(buttonSize);
        createFileButton = new JButton("Insert Create File Template");
        createFileButton.setMaximumSize(buttonSize);
        groupButton = new JButton("Insert Group Template");
        groupButton.setMaximumSize(buttonSize);
        renameButton = new JButton("Insert Rename Template");
        renameButton.setMaximumSize(buttonSize);

        // Set action listeners
        createFolderButton.addActionListener(e -> insertTemplate("create_folder_template"));
        createFileButton.addActionListener(e -> insertTemplate("create_file_template"));
        groupButton.addActionListener(e -> insertTemplate("group_template"));
        renameButton.addActionListener(e -> insertTemplate("rename_template"));

        // Input script area
        inputScriptArea = new JTextArea(20, 50);
        inputScriptArea.setLineWrap(true);
        inputScriptArea.setWrapStyleWord(true);
        JScrollPane inputScrollPane = new JScrollPane(inputScriptArea);
        inputScrollPane.setPreferredSize(new Dimension(580, 350));

        // Result area
        resultArea = new JTextArea(5, 50);
        resultArea.setEditable(false);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultScrollPane.setPreferredSize(new Dimension(580, 100));

        executeButton = new JButton("Run");

        // Layout configuration
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(createFolderButton);
        buttonPanel.add(createFileButton);
        buttonPanel.add(groupButton);
        buttonPanel.add(renameButton);

// Add the button panel to the frame
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.weighty = 0;
        frame.add(buttonPanel, gbc);

// Input Scroll Pane
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.7;
        frame.add(inputScrollPane, gbc);

// Execute Button
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        frame.add(executeButton, gbc);

// Result Scroll Pane
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.3;
        frame.add(resultScrollPane, gbc);
        executeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run();
            }
        });

        frame.setVisible(true);
    }
    private void run() {
        String input = inputScriptArea.getText();
        StringBuilder result = new StringBuilder();


            // Lexer and parser setup
            SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString(input));
            lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            SimpleFilesParser parser = new SimpleFilesParser(tokenStream);
            ParseTree parseTree = parser.program();
            ParseToASTVisitor parseToASTVisitor = new ParseToASTVisitor();
            Node parsedProgram = parseTree.accept(parseToASTVisitor);

            // Static check
            try {
            StaticCheck staticCheck = new StaticCheck();
            parsedProgram.accept(null, staticCheck);

            // Evaluation
            Evaluator evaluator = new Evaluator();
            parsedProgram.accept(null, evaluator);
            result.append("Test executed successfully.");

        } catch (Exception e) {
            result.append("Test execution failed: ").append(e.getMessage());
        }
        resultArea.setText(result.toString());
    }

    private void insertTemplate(String templateType) {
        String templateText = "";
        switch (templateType) {
            case "create_folder_template":
                templateText = "BEGIN\n"+
                        "INST create_folder_syntax -> :create_folder\n"
                        + "--> path = \n"
                        + "--> name = \n"
                        + "--> count = ;\n"
                        + "EXEC_INST create_folder_syntax;\n"
                        +"END\n";
                break;
            case "create_file_template":
                templateText = "BEGIN\n"+
                        "INST create_file_syntax -> :create_file\n"
                        + "--> name =\n"
                        + "--> name_file =\n"
                        + "--> path = \n"
                        + "--> count = ;\n"
                        + "EXEC_INST create_file_syntax;\n"
                        + "END\n";
                break;
            case "group_template":
                templateText = "BEGIN\n"
                        + "INST group_syntax -> :group\n"
                        + "--> group_target = \n"
                        + "--> path = \n"
                        + "--> mode =\n"
                        + "--> regex =\n"
                        + "--> contains =\n"
                        + "--> type =\n"
                        + "--> extension =\n"
                        + "--> modified_date = \n"
                        + "--> size =\n"
                        + "--> comparator = ;\n"
                        + "EXEC_INST group_syntax;\n"
                        + "END\n";
                break;
            case "rename_template":
                templateText = "BEGIN\n"+
                        "INST rename_syntax -> :rename\n"
                        + "--> path = \n"
                        + "--> mode =\n"
                        + "--> regex =\n"
                        + "--> contains =\n"
                        + "--> type =\n"
                        + "--> extension =\n"
                        + "--> modified_date =\n"
                        + "--> size =\n"
                        + "--> comparator = \n"
                        + "--> recursive = ;\n"
                        + "EXEC_INST rename_syntax;\n"
                        + "END\n";
                break;
            default:
                templateText = "Unknown template type";
        }
        inputScriptArea.insert(templateText, inputScriptArea.getCaretPosition());
    }

    public static void main(String[] args) {
        new UI();
    }
}