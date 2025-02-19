package com.monbat.components.header;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

public class Header extends Panel {
    public Header(String id) {
        super(id);

        add(new Link<Void>("homeLink") {
			@Override
			public void onClick() { }
		});

        add(new Link<Void>("aboutLink") {
			@Override
			public void onClick() { }
		});

        add(new Link<Void>("contactLink") {
			@Override
			public void onClick() { }
		});
    }
}