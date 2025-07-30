package com.monbat.components.menubar;

import com.monbat.components.center_area.CenterArea;
import com.monbat.pages.importfiles.ImportFiles;
import com.monbat.pages.salesOrders.DateRangePage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.springframework.web.client.RestTemplate;

import static com.monbat.components.ComponentIds.CENTER_AREA;
import static com.monbat.components.ComponentIds.CONTENT;

public class MenuBar extends Panel {
	private final transient RestTemplate restTemplate;

    public MenuBar(String id) {
        super(id);

		this.restTemplate = new RestTemplate();

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
				centerArea.replace(new DateRangePage(CONTENT));
				target.add(centerArea);
			}
		});
    }
}
