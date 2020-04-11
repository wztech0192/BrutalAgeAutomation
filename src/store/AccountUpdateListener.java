package store;

import store.Account;

public interface AccountUpdateListener {
    void onUpdate(Account acc);
}
