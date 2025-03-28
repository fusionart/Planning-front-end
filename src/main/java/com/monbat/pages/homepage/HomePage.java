package com.monbat.pages.homepage;

import com.monbat.components.base_page.BasePage;
import com.monbat.components.center_area.CenterArea;
import com.monbat.pages.tabs.TabContentPanel;
import org.apache.wicket.model.Model;

import java.io.Serial;
import java.io.Serializable;

import static com.monbat.components.ComponentIds.CENTER_AREA;
import static com.monbat.components.ComponentIds.CONTENT;

public class HomePage extends BasePage implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	public HomePage() {

		CenterArea centerArea = (CenterArea) get(CENTER_AREA);

		TabContentPanel tabPanel = new TabContentPanel(CONTENT,
				Model.of("Welcome"), // title model
				Model.of("This is the main content") // content model
		);

		centerArea.add(tabPanel);
	}
}
