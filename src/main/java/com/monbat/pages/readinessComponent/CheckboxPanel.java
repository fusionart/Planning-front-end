package com.monbat.pages.readinessComponent;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.Set;

public class CheckboxPanel extends Panel {
    public CheckboxPanel(String id, IModel<?> model, Set<?> selectedItems) {
        super(id);

        add(new AjaxCheckBox("checkbox", Model.of(selectedItems.contains(model.getObject()))) {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
//                if (getModelObject()) {
//                    selectedItems.add(model.getObject());
//                } else {
//                    selectedItems.remove(model.getObject());
//                }
            }
        });
    }
}
