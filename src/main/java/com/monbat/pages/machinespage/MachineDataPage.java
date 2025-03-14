package com.monbat.pages.machinespage;

import com.monbat.models.entities.MachineData;
import com.monbat.services.MachineDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MachineDataPage extends Panel {
    private final WebMarkupContainer contentContainer;

    public MachineDataPage(String id) {
        super(id);

        contentContainer = new WebMarkupContainer("contentContainer");
        contentContainer.setOutputMarkupId(true);
        contentContainer.setOutputMarkupPlaceholderTag(true);
        add(contentContainer);

        // Sample data
        List<MachineData> data = Arrays.asList(
                new MachineData("ASSUMER", "ASSUMERY", "Assembly Line", createWeeklyData(7, 21)),
                new MachineData("ASSUMER", "SUB_ASSEMB", "Subnominated Assembly", createWeeklyData(26, 62))
                // Add other machine data here
        );

        MachineDataProvider provider = new MachineDataProvider(data);
        List<IColumn<MachineData, String>> columns = MachineDataColumns.createColumns();

        DataTable<MachineData, String> table = new DataTable<>("table", columns, provider, 20);
        table.addTopToolbar(new NavigationToolbar(table));
        table.addTopToolbar(new HeadersToolbar<>(table, null));

        contentContainer.add(table);
    }

    private Map<String, Integer> createWeeklyData(int start, int end) {
        Map<String, Integer> weeklyData = new HashMap<>();
        for (int i = start; i <= end; i++) {
            weeklyData.put("W. " + i, i * 10); // Example data
        }
        return weeklyData;
    }
}
