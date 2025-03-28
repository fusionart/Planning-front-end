package com.monbat.pages.productionOrders;

import com.monbat.components.genericTable.ColumnDefinition;
import com.monbat.components.genericTable.GenericDataTablePanel;
import com.monbat.components.genericTable.PropertyColumnDefinition;
import com.monbat.models.dto.sap.ProductionOrderDto;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.monbat.utils.Constants.MAIN_ADDRESS;

public class ProductionOrderPanel extends Panel {
    private final transient RestTemplate restTemplate;

    public ProductionOrderPanel(String id) {
        super(id);
        setOutputMarkupId(true);

        restTemplate = new RestTemplate();

        List<ProductionOrderDto> result = fetchProductionOrders(); // Your list

        IModel<Collection<ProductionOrderDto>> model = () -> result;

        // Define columns
        List<ColumnDefinition<ProductionOrderDto>> columns = Arrays.asList(
                // Checkbox column would need a custom column definition
                new PropertyColumnDefinition<>("Material", "material", true, false),
                new PropertyColumnDefinition<>("Material desc", "materialDescription", true, false),
                new PropertyColumnDefinition<>("Prod. order", "productionOrder", true, false),
                new PropertyColumnDefinition<>("Prod. plant", "productionPlant"),
                new PropertyColumnDefinition<>("Is released", "orderIsReleased", true, false),
                new PropertyColumnDefinition<>("Is scheduled", "orderIsScheduled"),
                new PropertyColumnDefinition<>("Prod. supervisor", "productionSupervisor"),
                new PropertyColumnDefinition<>("Prod. version", "productionVersion"),
                new PropertyColumnDefinition<>("Work center", "workCenter", true, false),
                new PropertyColumnDefinition<>("Start date", "mfgOrderScheduledStartDate", true, false),
                new PropertyColumnDefinition<>("Start time", "mfgOrderScheduledStartTime"),
                new PropertyColumnDefinition<>("End date", "mfgOrderScheduledEndDate"),
                new PropertyColumnDefinition<>("End time", "mfgOrderScheduledEndTime"),
                new PropertyColumnDefinition<>("Prod. unit", "productionUnit"),
                new PropertyColumnDefinition<>("Total qty", "totalQuantity"),
                new PropertyColumnDefinition<>("Confirmed qty", "mfgOrderConfirmedYieldQty")
        );

        // Create filter function
        GenericDataTablePanel<ProductionOrderDto> dataTablePanel =
                new GenericDataTablePanel<>(
                        "table",
                        model,
                        columns,
                        detail -> Arrays.asList(
                                detail.getMaterial() != null ? detail.getMaterial() : "",
                                detail.getWorkCenter() != null ? detail.getWorkCenter() : ""
                        )
                );

        add(dataTablePanel);
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
            LoggerFactory.getLogger(getClass()).error("Error fetching production orders data", e);
            return Collections.emptyList();
        }
    }
}
