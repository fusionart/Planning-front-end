package com.monbat.services;

import com.monbat.models.entities.MachineData;
import org.apache.wicket.markup.repeater.data.ListDataProvider;

import java.util.List;

public class MachineDataProvider extends ListDataProvider<MachineData> {
    public MachineDataProvider(List<MachineData> data) {
        super(data);
    }

    @Override
    protected List<MachineData> getData() {
        return super.getData();
    }
}
