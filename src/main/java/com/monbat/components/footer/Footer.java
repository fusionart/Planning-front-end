package com.monbat.components.footer;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

public class Footer extends Panel {
    public Footer(String id) {
        super(id);

        add(new Link<Void>("privacyLink") {
            @Override
            public void onClick() { }
        });

        add(new Link<Void>("termsLink") {
            @Override
            public void onClick() { }
        });
    }
}
