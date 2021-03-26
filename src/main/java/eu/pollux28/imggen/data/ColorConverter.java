package eu.pollux28.imggen.data;

public interface ColorConverter<T> {
    public T GetValue(int color);
    public T GetDefaultValue(int biomeX, int biomeZ);
}
