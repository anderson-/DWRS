///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package s3f.dwrs_o;
//
//import java.io.InputStream;
//import javax.swing.Icon;
//import javax.swing.JMenu;
//import javax.swing.JMenuItem;
//import javax.swing.JTextArea;
//import javax.swing.UIManager;
//import s3f.core.code.CodeEditorTab;
//import s3f.core.plugin.Configurable;
//import s3f.core.plugin.Data;
//import s3f.core.plugin.Plugabble;
//import s3f.core.project.FileCreator;
//import s3f.core.project.Project;
//import s3f.core.project.ProjectTreeTab;
//import s3f.core.ui.GUIBuilder;
//import s3f.core.ui.ToolBarButton;
//import s3f.core.ui.tab.TabProperty;
//
///**
// *
// * @author antunes
// */
//public class UI extends GUIBuilder {
//
//    public UI() {
//        super("DWRS");
//    }
//
//    public static class TMPElement implements s3f.core.project.Element, Configurable {
//
//        private final String name;
//        private final Icon icon;
//        private final String cname = "asd" + (int) (Math.random() * 10);
//        public final Data data;
//
//        public TMPElement(String name, Icon icon) {
//            this.name = name;
//            this.icon = icon;
//            data = new Data("name", "s3f.whut", "sei lá");
//            TabProperty.put(data, name, icon, "tt", new JTextArea("isso é um teste.. " + name));
//        }
//
//        @Override
//        public String getName() {
//            return name;
//        }
//
//        @Override
//        public Icon getIcon() {
//            return icon;
//        }
//
//        @Override
//        public String toString() {
//            return name;
//        }
//
//        @Override
//        public void save(FileCreator fileCreator) {
//
//        }
//
//        @Override
//        public CategoryData getCategoryData() {
//            //return new CategoryData(cname, ".x", UIManager.getIcon("FileView.computerIcon"), this);
//            return new CategoryData(cname, ".x", null, this);
//        }
//
//        @Override
//        public s3f.core.project.Element load(InputStream stream) {
//            return null;
//        }
//
//        @Override
//        public Data getData() {
//            return data;
//        }
//
//        @Override
//        public void init() {
//            
//        }
//
//        @Override
//        public Plugabble createInstance() {
//            return new TMPElement("new", null);
//        }
//
//    }
//
//    @Override
//    public void init() {
//
//        this.addToolbarComponent(new ToolBarButton().getJComponent(), 0);
//        this.addToolbarComponent(new ToolBarButton().getJComponent(), 0);
//        this.addToolbarComponent(new ToolBarButton().getJComponent(), 600);
//        this.addToolbarComponent(new ToolBarButton().getJComponent(), 600);
//
//        Project project = new Project(getString("project.name"));
//
//        for (int i = 0; i < 10; i++) {
//            project.addElement(new TMPElement("asdl", UIManager.getIcon("FileView.fileIcon")));
//        }
//
//        this.addTab(new ProjectTreeTab(project), 2);
//
//        ProjectTreeTab projectTreeTab = new ProjectTreeTab(project);
//        this.addTab(projectTreeTab, 2);
//        this.addTab(new CodeEditorTab("javascript"), 2);
//
//        JMenu jMenu = new JMenu();
//
//        jMenu.setText("JIFI");
//        jMenu.add(new JMenuItem("asd"));
//        this.addMenubar(jMenu, 0);
//    }
//
//}
