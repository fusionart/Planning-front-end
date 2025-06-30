package com.monbat.pages.productionOrders;

import com.inmethod.grid.IDataSource;
import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.column.PropertyColumn;
import com.inmethod.grid.datagrid.DataGrid;
import com.inmethod.grid.datagrid.DefaultDataGrid;
import com.monbat.models.dto.sap.ProductionOrderDto;
import org.apache.commons.codec.binary.Base64;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductionOrderPanel extends Panel {

    private static final String MAIN_ADDRESS = "http://localhost:8080/";
    private final transient RestTemplate restTemplate = new RestTemplate();
    private final IModel<List<ProductionOrderDto>> dataModel;

    public ProductionOrderPanel(String id) {
        super(id);
        setOutputMarkupId(true);

        // Initialize model first
        this.dataModel = createProductionOrderModel();
        setDefaultModel(dataModel); // Set as default model

        WebMarkupContainer gridContainer = new WebMarkupContainer("gridContainer");
        gridContainer.setOutputMarkupId(true);
        add(gridContainer);

        IDataSource<ProductionOrderDto> dataSource = new ProductionOrderDataSource(dataModel);

        List<IGridColumn<IDataSource<ProductionOrderDto>, ProductionOrderDto, String>> columns = createColumns();

        DataGrid<IDataSource<ProductionOrderDto>, ProductionOrderDto, String> dataGrid =
                new DefaultDataGrid<>("dataGrid", dataSource, columns);

        dataGrid.setRowsPerPage(20);
        dataGrid.setOutputMarkupId(true);
        gridContainer.add(dataGrid);
    }

    private IModel<List<ProductionOrderDto>> createProductionOrderModel() {
        return new LoadableDetachableModel<>() {
            @Override
            protected List<ProductionOrderDto> load() {
                List<ProductionOrderDto> orders = fetchProductionOrders();
                LoggerFactory.getLogger(ProductionOrderPanel.class)
                        .info("Loaded {} production orders", orders.size());
                return orders;
            }

            @Override
            protected void onDetach() {
                super.onDetach();
                LoggerFactory.getLogger(ProductionOrderPanel.class)
                        .debug("Production order model detached");
            }
        };
    }

    private List<IGridColumn<IDataSource<ProductionOrderDto>, ProductionOrderDto, String>> createColumns() {
        List<IGridColumn<IDataSource<ProductionOrderDto>, ProductionOrderDto, String>> columns = new ArrayList<>();

        for (Field field : ProductionOrderDto.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (Modifier.isTransient(field.getModifiers())) continue;

            String name = field.getName();
            columns.add(new PropertyColumn<>(
                    Model.of(capitalize(name)),
                    name
            ));
        }
        return columns;
    }

    private List<ProductionOrderDto> fetchProductionOrders() {
        String apiUrl = MAIN_ADDRESS + "api/sap/getProductionOrders";
        Base64 base64 = new Base64();
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam("username", new String(base64.encode("niliev".getBytes())))
                    .queryParam("password", new String(base64.encode("21Zaq12wsx!!".getBytes())))
                    .queryParam("reqDelDateBegin", LocalDate.of(2025, Month.MARCH, 1).atStartOfDay())
                    .queryParam("reqDelDateEnd", LocalDate.of(2025, Month.MARCH, 31).atStartOfDay());

            ResponseEntity<List<ProductionOrderDto>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("Error fetching production orders", e);
            return Collections.emptyList();
        }
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        LoggerFactory.getLogger(ProductionOrderPanel.class)
                .debug("ProductionOrderPanel initialized");
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        // Safe model detachment
        if (dataModel != null) {
            dataModel.detach();
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        // Clean up model when panel detaches
        if (dataModel != null) {
            dataModel.detach();
        }
    }
}