package eu.pollux28.imggen.data;

public interface ColorConverter<T> {
    T GetValue(int color);
    T GetDefaultValue(int biomeX, int biomeZ);
}
