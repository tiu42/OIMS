open module com.oims.oims {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.base;
    requires transitive javafx.graphics;
    exports com.oims;
    exports com.oims.views;
}