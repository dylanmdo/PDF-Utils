module org.pdfutils {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.pdfbox;
    requires org.jfxtras.styles.jmetro;
    requires java.desktop;

    opens org.pdfutils to javafx.fxml;
    opens org.pdfutils.controller to javafx.fxml;
    exports org.pdfutils.controller;



}
