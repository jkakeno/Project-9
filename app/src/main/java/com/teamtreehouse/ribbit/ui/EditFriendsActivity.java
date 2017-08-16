package com.teamtreehouse.ribbit.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.teamtreehouse.ribbit.R;
import com.teamtreehouse.ribbit.adapters.UserAdapter;
import com.teamtreehouse.ribbit.models.Query;
import com.teamtreehouse.ribbit.models.Relation;
import com.teamtreehouse.ribbit.models.User;
import com.teamtreehouse.ribbit.models.callbacks.FindCallback;
import com.teamtreehouse.ribbit.models.callbacks.SaveCallback;

import java.util.List;

public class EditFriendsActivity extends Activity {

    protected Relation<User> mFriendsRelation;
    protected User mCurrentUser;
    protected GridView mGridView;

    public static final String TAG = EditFriendsActivity.class.getSimpleName();

    protected List<User> mUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.user_grid);
        // Show the Up button in the action bar.
        setupActionBar();

        mGridView = (GridView) findViewById(R.id.friendsGrid);
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
        mGridView.setOnItemClickListener(mOnItemClickListener);

        TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();

        mCurrentUser = User.getCurrentUser();
        mFriendsRelation = new Relation<>();

        setProgressBarIndeterminateVisibility(true);

        Query<User> query = User.getQuery();
        query.orderByAscending(User.KEY_USERNAME);
        query.setLimit(1000);
        query.findInBackground(new FindCallback<User>() {
            @Override
            public void done(List<User> users, Exception e) {
                setProgressBarIndeterminateVisibility(false);

                if (e == null) {
                    // Success
                    mUsers = users;
                    String[] usernames = new String[mUsers.size()];
                    Log.d(TAG, String.valueOf(mUsers.size()));
                    int i = 0;
                    for (User user : mUsers) {
                        usernames[i] = user.getUsername();
                        i++;
                    }
                    if (mGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(EditFriendsActivity.this, mUsers);
                        mGridView.setAdapter(adapter);
                    } else {
                        ((UserAdapter) mGridView.getAdapter()).refill(mUsers);
                    }

                } else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }
//Add to pass the mFriendRelation list to FriendFragment.java
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "mFriendRelation size: "+mFriendsRelation.size());
        Intent intent = new Intent(EditFriendsActivity.this,FriendsFragment.class);
        intent.putExtra("FRIEND_LIST", (Parcelable) mFriendsRelation);
        startActivity(intent);

    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG,"onOptionItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//DON"T NEED THIS METHOD TO ADD CHECK MARKS
    private void addFriendCheckmarks() {
        Log.d(TAG,"addFriendCheckmarks");
        mFriendsRelation.getQuery().findInBackground(new FindCallback<User>() {
            @Override
            public void done(List<User> friends, Exception e) {
                if (e == null) {
                    // list returned - look for a match
                    for (int i = 0; i < mUsers.size(); i++) {
                        User user = mUsers.get(i);
                        Log.d(TAG,"user is: " + user.getUsername());
                        for (User friend : friends) {
                            Log.d(TAG,"friend is: " + friend.getUsername());
                            if (friend.getObjectId().equals(user.getObjectId())) {
                                Log.d(TAG,"friend equals user");
                                mGridView.setItemChecked(i, true);
                            }
                        }
                    }
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    protected OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
            ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);

            if (mGridView.isItemChecked(position)) {
                // add the friend
                mFriendsRelation.add(mUsers.get(position));
                checkImageView.setVisibility(View.VISIBLE);
                Log.d(TAG, "add user " + mUsers.get(position).getUsername() + " to mFriendRelation");
                Log.d(TAG, "mFriendRelation size: "+mFriendsRelation.size());

            } else {
                // remove the friend
                mFriendsRelation.remove(mUsers.get(position));
                checkImageView.setVisibility(View.INVISIBLE);
                Log.d(TAG, "remove user " + mUsers.get(position).getUsername() + " from mFriendRelation");
                Log.d(TAG, "mFriendRelation size: "+mFriendsRelation.size());
            }

            mCurrentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(Exception e) {
                    if (e != null) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            });

        }
    };
}










