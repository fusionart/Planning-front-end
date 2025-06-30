package com.monbat.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalTime;

public class LocalTimeAdapter extends TypeAdapter<LocalTime> {
    @Override
    public void write(JsonWriter out, LocalTime value) throws IOException {
        out.value(value != null ? value.toString() : null);
    }

    @Override
    public LocalTime read(JsonReader in) throws IOException {
        String value = in.nextString();
        return value != null ? LocalTime.parse(value) : null;
    }
}
