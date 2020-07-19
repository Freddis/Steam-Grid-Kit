package kit.utils;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.function.Supplier;

/**
 *  Shortcut to use in Lambda functions
 * @param <T>
 */
public class SimpleObservableValue<T> implements ObservableValue<T> {

    private final Supplier<T> valueSupplier;

    public SimpleObservableValue(Supplier<T> valueSupplier)
    {
        this.valueSupplier = valueSupplier;
    }
    @Override
    public void addListener(ChangeListener<? super T> listener) {

    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {

    }

    @Override
    public T getValue() {
        return valueSupplier.get();
    }


    @Override
    public void addListener(InvalidationListener listener) {

    }

    @Override
    public void removeListener(InvalidationListener listener) {

    }
}
