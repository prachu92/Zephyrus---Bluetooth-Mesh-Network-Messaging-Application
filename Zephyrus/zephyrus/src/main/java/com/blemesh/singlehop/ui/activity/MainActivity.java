package com.blemesh.singlehop.ui.activity;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.SupportMenuInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import com.nispok.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import com.blemesh.sdk.app.ui.PeerFragment;
import com.blemesh.singlehop.PrefsManager;
import com.blemesh.singlehop.R;
import com.blemesh.singlehop.ui.fragment.QuoteWritingFragment;
import com.blemesh.singlehop.ui.fragment.WelcomeFragment;
import com.blemesh.sdk.mesh_graph.Peer;
import timber.log.Timber;

/**
 * An Activity illustrating use of BleMesh's {@link PeerFragment} to facilitate
 * simple synchronous data exchange.
 */
public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener,
        WelcomeFragment.WelcomeFragmentListener,
        QuoteWritingFragment.WritingFragmentListener,
        PeerFragment.PeerFragmentListener {

    private static final String SERVICE_NAME = "BleSingleDemo";
    private TextView logView;
    private Toolbar toolbar;
    private MenuItem receiveMenuItem;
    byte[] payloadToShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        assert toolbar != null;
        toolbar.inflateMenu(R.menu.activity_main);

        receiveMenuItem = toolbar.getMenu().findItem(R.id.action_receive);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().popBackStack();
            }
        });

        logView = (TextView)findViewById(R.id.ble_mesh_log);

        Fragment baseFragment;

        if (PrefsManager.needsUsername(this)) {
            baseFragment = new WelcomeFragment();
            receiveMenuItem.setVisible(false);
        } else
            baseFragment = new QuoteWritingFragment();


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, baseFragment)
                .commit();

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int numEntries = getSupportFragmentManager().getBackStackEntryCount();
                if (numEntries == 0) {
                    // Back at "Home" State (WritingFragment)
                    receiveMenuItem.setVisible(true);
                    showSubtitle(false);
                    toolbar.setTitle("");
                    toolbar.setNavigationIcon(null);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater _MenuInflater = new SupportMenuInflater(this);
        _MenuInflater.inflate(R.menu.activity_main,menu);
        return true;
    }

    private void showSubtitle(boolean doShow) {
        if (doShow) {
            toolbar.setSubtitle(getString(R.string.as_name, PrefsManager.getUsername(this)));
        } else
            toolbar.setSubtitle("");
    }

    @Override
    public void onUsernameSelected(String username) {
        PrefsManager.setUsername(this, username);
        showWritingFragment();
    }

    @Override
    public void onShareRequested(String quote) {
        logView.setText("");
        HashMap<String, Object> dataToShare = new HashMap<>();
        dataToShare.put("quote", quote);

        payloadToShare = new JSONObject(dataToShare).toString().getBytes();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, PeerFragment.toSend(payloadToShare, PrefsManager.getUsername(this), SERVICE_NAME))
                .addToBackStack(null)
                .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        receiveMenuItem.setVisible(false);
        toolbar.setNavigationIcon(R.mipmap.ic_cancel);
        toolbar.setTitle(getString(R.string.discovering));
        showSubtitle(true);
    }

    @Override
    public void onReceiveButtonClick() {
        logView.setText("");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, PeerFragment.toReceive(PrefsManager.getUsername(this), SERVICE_NAME))
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        receiveMenuItem.setVisible(false);
        toolbar.setNavigationIcon(R.mipmap.ic_cancel);
        toolbar.setTitle(getString(R.string.discovering));
        showSubtitle(true);
    }

    public void onBothSendAndReceive(String quote){
        logView.setText("");
        HashMap<String, Object> dataToShare = new HashMap<>();
        dataToShare.put("quote", quote);
        payloadToShare = new JSONObject(dataToShare).toString().getBytes();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, PeerFragment.toSendAndReceive(PrefsManager.getUsername(this), SERVICE_NAME))
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        receiveMenuItem.setVisible(false);
        toolbar.setNavigationIcon(R.mipmap.ic_cancel);
        toolbar.setTitle(getString(R.string.sending_quote));
        showSubtitle(true);
    }

    private void showWritingFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, new QuoteWritingFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        receiveMenuItem.setVisible(true);
        showSubtitle(false);
    }

    @Override
    public void onDataReceived(@NonNull PeerFragment fragment,
                               @Nullable byte[] data,
                               @NonNull String sourceAddress,
                               @NonNull Peer sender) {

        // In this example app, we're only using the headers data
        if (data != null) {
            try {
                JSONObject json = new JSONObject(new String(data));
                Timber.d("Got data from %s", sender.getAlias());
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.quote_received))
                        .setMessage(getString(R.string.quote_and_author,
                                json.get("quote")))
                        .setPositiveButton(getString(R.string.ok), null)
                        .show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDataSent(@NonNull PeerFragment fragment,
                           @Nullable byte[] data,
                           @NonNull Peer recipient,
                           @NonNull Peer desc) {

        // In this example app, we're only using the headers data
        if (data != null) {
            Timber.d("Sent data to %s, desc is %s", recipient.getAlias(),desc.getAlias());
            Snackbar.with(getApplicationContext())
                    .text(R.string.quote_sent)
                    .show(this);
        }
    }

    @Override
    public void onDataRequestedForPeer(@NonNull PeerFragment fragment, @NonNull Peer recipient) {
        // unused. If we were using PeerFragment in send and receive mode, we would
        // deliver data for peer:
        // fragment.sendDataToPeer("Some dynamic data".getBytes(), recipient);
        fragment.sendDataToPeer(payloadToShare,recipient);

    }

    @Override
    public void onFinished(@NonNull PeerFragment fragment, Exception exception) {
        // Remove last fragment
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onNewLog(@NonNull String logText) {
        logView.append(logText + "\n");
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_receive:
                onReceiveButtonClick();
                return true;
        }

        return false;
    }
}
