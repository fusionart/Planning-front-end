package com.monbat.components.menubar;

import com.monbat.components.center_area.CenterArea;
import com.monbat.pages.importfiles.ImportFiles;
import com.monbat.pages.machinespage.MachineDataPage;
import com.monbat.pages.tabs.DynamicTabsPage;
import com.monbat.pages.timeline.TimeLine;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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
				centerArea.replace(new DynamicTabsPage(CONTENT));
				target.add(centerArea);
			}
		});

		add(new AjaxLink<Void>("machineTable") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				CenterArea centerArea = (CenterArea) getPage().get(CENTER_AREA);
				centerArea.replace(new MachineDataPage(CONTENT));
				target.add(centerArea);
			}
		});

		add(new AjaxLink<Void>("timeLine") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				CenterArea centerArea = (CenterArea) getPage().get(CENTER_AREA);
				centerArea.replace(new TimeLine(CONTENT));
				target.add(centerArea);
			}
		});

		add(new AjaxLink<Void>("loadDataFromSAP") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				String apiUrl = "http://localhost:8080/api/sap/getProductionOrders";

				try {
					ResponseEntity<List<String>> response = restTemplate.exchange(
							apiUrl,
							HttpMethod.GET,
							null,
							new ParameterizedTypeReference<>() {
							}
					);
				} catch (Exception e) {
					// Log the error and return an empty list
					LoggerFactory.getLogger(getClass()).error("Error fetching readiness data", e);
				}
			}
		});
    }
}
