package com.monbat.pages.productionOrders;

import com.inmethod.grid.IDataSource;
import com.monbat.models.dto.sap.ProductionOrderDto;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.Collections;
import java.util.List;

public class ProductionOrderDataSource implements IDataSource<ProductionOrderDto> {

    private final IModel<List<ProductionOrderDto>> model;

    public ProductionOrderDataSource(IModel<List<ProductionOrderDto>> model) {
        this.model = model;
    }

    @Override
    public void query(IQuery query, IQueryResult<ProductionOrderDto> result) {
        List<ProductionOrderDto> orders = model.getObject();

        if (orders == null || orders.isEmpty()) {
            result.setItems(Collections.emptyIterator());  // Java 7/8 compatible
            result.setTotalCount(0);
            return;
        }

        int from = (int) query.getFrom();
        int count = (int) query.getCount();
        int to = Math.min(from + count, orders.size());

        result.setItems(orders.subList(from, to).iterator());
        result.setTotalCount(orders.size());
    }

    @Override
    public IModel<ProductionOrderDto> model(ProductionOrderDto productionOrderDto) {
        return Model.of(productionOrderDto);
    }

    @Override
    public void detach() {
        model.detach();
    }
}
