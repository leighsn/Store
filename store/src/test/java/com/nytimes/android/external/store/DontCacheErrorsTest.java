package com.nytimes.android.external.store;

import com.nytimes.android.external.store.base.Fetcher;
import com.nytimes.android.external.store.base.Store;
import com.nytimes.android.external.store.base.impl.BarCode;
import com.nytimes.android.external.store.base.impl.StoreBuilder;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import rx.Observable;

public class DontCacheErrorsTest {

    private boolean shouldThrow;
    private Store<Integer> store;

    @Before
    public void setUp() {
        store = StoreBuilder.<Integer>builder()
                .fetcher(new Fetcher<Integer>() {
                    @Nonnull
                    @Override
                    public Observable<Integer> fetch(BarCode barCode) {
                        return Observable.fromCallable(new Callable<Integer>() {
                            @Override
                            public Integer call() {
                                if (shouldThrow) {
                                    throw new RuntimeException();
                                } else {
                                    return 0;
                                }
                            }
                        });
                    }
                })
                .open();
    }

    @Test
    public void testStoreDoesntCacheErrors() throws InterruptedException {
        BarCode barcode = new BarCode("bar", "code");

        shouldThrow = true;
        store.get(barcode).test()
                .awaitTerminalEvent()
                .assertError(Exception.class);

        shouldThrow = false;
        store.get(barcode).test()
                .awaitTerminalEvent()
                .assertNoErrors();
    }
}