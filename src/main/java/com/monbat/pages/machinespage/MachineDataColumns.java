package com.monbat.pages.machinespage;

import com.monbat.models.entities.MachineData;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.List;

public class MachineDataColumns {
    public static List<IColumn<MachineData, String>> createColumns() {
        List<IColumn<MachineData, String>> columns = new ArrayList<>();
        columns.add(new PropertyColumn<>(Model.of("Grouping"), "grouping"));
        columns.add(new PropertyColumn<>(Model.of("Machine"), "machine"));
        columns.add(new PropertyColumn<>(Model.of("Machine Description"), "machineDescription"));

        // Add weekly data columns dynamically if needed
        for (int i = 1; i <= 21; i++) {
            final String week = "W. " + i;
            columns.add(new PropertyColumn<>(Model.of(week), "weeklyData." + week));
        }

        return columns;
    }
}