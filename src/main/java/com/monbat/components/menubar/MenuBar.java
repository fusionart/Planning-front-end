package com.monbat.components.menubar;

import com.monbat.components.center_area.CenterArea;
import com.monbat.pages.grid.Grid;
import com.monbat.pages.importfiles.ImportFiles;
import com.monbat.pages.machinespage.MachineDataPage;
import com.monbat.pages.newTimeline.TimelinePage;
import com.monbat.pages.productionOrders.ProductionOrderPanel;
import com.monbat.pages.salesOrders.SalesOrderDynamicTabsPage;
import com.monbat.pages.test.DateRangePage;
import com.monbat.pages.timeline.TimeLine;
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

		add(new AjaxLink<Void>("newTimeLine") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				CenterArea centerArea = (CenterArea) getPage().get(CENTER_AREA);
				centerArea.replace(new TimelinePage(CONTENT));
				target.add(centerArea);
			}
		});

		add(new AjaxLink<Void>("grid") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				CenterArea centerArea = (CenterArea) getPage().get(CENTER_AREA);
				centerArea.replace(new Grid(CONTENT));
				target.add(centerArea);
			}
		});

		add(new AjaxLink<Void>("loadProductionOrdersFromSAP") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				CenterArea centerArea = (CenterArea) getPage().get(CENTER_AREA);
				centerArea.replace(new ProductionOrderPanel(CONTENT));
				target.add(centerArea);
			}
		});

		add(new AjaxLink<Void>("loadSalesOrdersFromSAP") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				CenterArea centerArea = (CenterArea) getPage().get(CENTER_AREA);
				centerArea.replace(new SalesOrderDynamicTabsPage(CONTENT));
				target.add(centerArea);
			}
		});
    }
}
