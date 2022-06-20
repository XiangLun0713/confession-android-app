package me.xianglun.confession_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.xianglun.confession_app.fragment.AdminFragment;
import me.xianglun.confession_app.fragment.DeletePostFragment;
import me.xianglun.confession_app.fragment.ReportedPostFragment;

public class AdminMainActivity extends AppCompatActivity {

    // declare reference variables
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private FirebaseAuth mFirebaseAuth;

    private AdminFragment adminFragment;
    private ReportedPostFragment reportedPostFragment;
    private DeletePostFragment deletePostFragment;
    private boolean doubleBackToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // Instantiate reference variables
        mToolbar = findViewById(R.id.admin_toolbar);
        mViewPager = findViewById(R.id.view_pager);
        mTabLayout = findViewById(R.id.tab_layout);
        mFirebaseAuth = FirebaseAuth.getInstance();
        adminFragment = new AdminFragment();
        reportedPostFragment = new ReportedPostFragment();
        deletePostFragment = new DeletePostFragment();

        // setting up the tab layout and the view pager adapter
        mTabLayout.setupWithViewPager(mViewPager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(reportedPostFragment, "Reported");
        viewPagerAdapter.addFragment(deletePostFragment, "Remove");
        viewPagerAdapter.addFragment(adminFragment, "Admin");
        mViewPager.setAdapter(viewPagerAdapter);
        Objects.requireNonNull(mTabLayout.getTabAt(0)).setIcon(R.drawable.ic_warning_icon_24);
        Objects.requireNonNull(mTabLayout.getTabAt(1)).setIcon(R.drawable.ic_delete_icon_24);
        Objects.requireNonNull(mTabLayout.getTabAt(2)).setIcon(R.drawable.ic_people_icon_24);

        // configure the action bar
        mToolbar.setTitle(Html.fromHtml("<font color=\"#444444\">&nbsp;&nbsp Admin </font>"));
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_star_icon_24);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.admin_main_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            logOut();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to log out", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.admin_logout_menu_item) {
            logOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logOut() {
        if (mFirebaseAuth.getCurrentUser() != null) {
            mFirebaseAuth.signOut();
            Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminMainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }
}