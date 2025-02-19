package com.monbat.components.menubar;

import com.monbat.components.center_area.CenterArea;
import com.monbat.pages.importfiles.ImportFiles;
import com.monbat.pages.tabs.DynamicTabsPage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import static com.monbat.components.ComponentIds.*;

public class MenuBar extends Panel {
    public MenuBar(String id) {
        super(id);

		add(new AjaxLink<Void>("toImports") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				CenterArea centerArea = (CenterArea) getPage().get(CENTER_AREA);
				centerArea.replace(new ImportFiles(CONTENT));
				target.add(centerArea);
			}
		});

		add(new AjaxLink<Void>("toTabs") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				CenterArea centerArea = (CenterArea) getPage().get(CENTER_AREA);
				centerArea.replace(new DynamicTabsPage(CONTENT));
				target.add(centerArea);
			}
		});
    }
}
