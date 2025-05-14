package com.monbat.pages.productionOrders;

import com.monbat.models.dto.sap.ProductionOrderDto;
import com.shieldui.wicket.datasource.DataSourceOptions;
import com.shieldui.wicket.grid.Grid;
import com.shieldui.wicket.grid.GridOptions;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.monbat.utils.Constants.MAIN_ADDRESS;

public class ProductionOrderPanel extends Panel {
    private final transient RestTemplate restTemplate;

    public ProductionOrderPanel(String id) {
        super(id);
        setOutputMarkupId(true);

        restTemplate = new RestTemplate();

        List<ProductionOrderDto> productionOrders = fetchProductionOrders();

        final Grid grid = new Grid("gridContainer");
        GridOptions options = grid.getOptions();

        // Set data directly
        DataSourceOptions dataSourceOptions = new DataSourceOptions();
        dataSourceOptions.setData(productionOrders);

        options.setDataSource(dataSourceOptions);
        options.setColumns(createColumnsFromEntityClass(ProductionOrderDto.class));

        // Enable features
        // Paging options
        GridOptions.Paging paging = new GridOptions.Paging();
        paging.setPageSize(10);
        options.setPaging(paging);
        options.setSorting(new GridOptions.Sorting());
        options.setFiltering(new GridOptions.Filtering());
        options.setSelection(new GridOptions.Selection());

        add(grid);
    }

    private List<GridOptions.ColumnOption> createColumnsFromEntityClass(Class<?> entityClass) {
        List<GridOptions.ColumnOption> columns = new ArrayList<>();

        for (Field field : entityClass.getDeclaredFields()) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                    !java.lang.reflect.Modifier.isTransient(field.getModifiers())) {

                String fieldName = field.getName();

                GridOptions.ColumnOption column = new GridOptions.ColumnOption()
                        .setField(fieldName)
                        .setTitle(capitalizeFirstLetter(fieldName));

                if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                    column.setFormat("{{if " + fieldName + "}}Yes{{else}}No{{/if}}");
                }

                columns.add(column);
            }
        }

        return columns;
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private List<ProductionOrderDto> fetchProductionOrders() {
        String apiUrl = MAIN_ADDRESS + "api/sap/getProductionOrders";
        try {
            ResponseEntity<List<ProductionOrderDto>> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );
            return response.getBody();
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("Error fetching production orders", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        // Linking to the external CSS and JavaScript files
        response.render(CssHeaderItem.forUrl("https://www.shieldui.com/shared/components/latest/css/light/all.min.css"));
        response.render(JavaScriptHeaderItem.forUrl("https://www.shieldui.com/shared/components/latest/js/jquery-1.10.2.min.js"));
        response.render(JavaScriptHeaderItem.forUrl("https://www.shieldui.com/shared/components/latest/js/shieldui-all.min.js"));
    }

}
