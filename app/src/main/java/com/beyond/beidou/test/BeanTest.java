package com.beyond.beidou.test;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class BeanTest {
        private String property = "00000000-0000-0000-0000-000000000000";
        private PropertyChangeSupport changeSupport = new PropertyChangeSupport(
                this);

        public void setProperty(String newValue) {
            String oldValue = this.property;
            this.property = newValue;
            changeSupport.firePropertyChange("property", oldValue, newValue);
        }
        public String getProperty(){
            return property;
        }

    public void addPropertyChangeListener(PropertyChangeListener l) {
            changeSupport.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            changeSupport.removePropertyChangeListener(l);
        }
}