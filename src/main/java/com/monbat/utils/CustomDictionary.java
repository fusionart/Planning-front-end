package com.monbat.utils;

import com.monbat.utils.enums.TableNames;

import java.util.HashMap;

public class CustomDictionary {
    public static HashMap<String, String> getDictionary(TableNames table){
        switch (table){
            case CAPACITY_SUMMARY -> {
                return getCapacitySummaryDictionary();
            }
            case SALES_ORDER_WITH_ITEM -> {
                return getSalesOrderWithItemsDictionary();
            }
            default -> {
                return null;
            }
        }
    }

    private static HashMap<String, String> getSalesOrderWithItemsDictionary() {
        HashMap<String, String> dictionary = new HashMap<>();
        dictionary.put("salesOrderNumber", "Заявка");
        dictionary.put("soldToParty", "Клиент");
        dictionary.put("requestedDeliveryDate", "Заявена дата");
        dictionary.put("requestedDeliveryWeek", "Заявена седмица");
        dictionary.put("material", "Материал");
        dictionary.put("requestedQuantity", "Заявено количество");
        dictionary.put("requestedQuantityUnit", "Мерна единица");
        dictionary.put("sdProcessStatus", "Статус");
        dictionary.put("plant", "Завод");
        dictionary.put("completeDelivery", "Без липси");
        dictionary.put("plannedOrder", "Планова поръчка");
        dictionary.put("productionOrder", "Производствена поръчка");
        dictionary.put("availableNotCharged", "Неформирани");
        dictionary.put("availableCharged", "Формирани");
        return dictionary;
    }

    private static HashMap<String, String> getCapacitySummaryDictionary() {
        HashMap<String, String> dictionary = new HashMap<>();
        dictionary.put("productionWorkshop", "Производствен цех");
        dictionary.put("workCenter", "Работен център");
        dictionary.put("description", "Описание");
        dictionary.put("quantity", "Седмично количество");
        dictionary.put("calculatedShifts", "Необходими работни смени");
        dictionary.put("personnel", "Персонал за смяна");
        dictionary.put("norm", "Седмичен капацитет");
        dictionary.put("produced", "Произведено количество");
        dictionary.put("workingDays", "Работен център - работни дни");
        dictionary.put("shifts", "Работен център - смени на ден");
        dictionary.put("enterShifts", "Редакция смени");
        return dictionary;
    }
}
