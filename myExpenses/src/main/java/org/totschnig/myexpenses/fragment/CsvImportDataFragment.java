package org.totschnig.myexpenses.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import org.apache.commons.csv.CSVRecord;
import org.totschnig.myexpenses.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by privat on 30.06.15.
 */
public class CsvImportDataFragment extends Fragment {
  public static final String KEY_DATASET = "KEY_DATASET";
  public static final int CELL_WIDTH = 100;
  private String[] fields;
  public static final int CHECKBOX_COLUMN_WIDTH = 60;
  public static final int CELL_MARGIN = 5;
  private RecyclerView mRecyclerView;
  private RecyclerView.Adapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;
  private ArrayList<CSVRecord> mDataset;
  private Map<Integer,Integer> columnToFieldMap = new HashMap<>();


  public static CsvImportDataFragment newInstance() {
    return new CsvImportDataFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    fields = new String[] {
        "Ignore",
        getString(R.string.date),
        getString(R.string.payer_or_payee),
        getString(R.string.amount),
        getString(R.string.comment),
        getString(R.string.category),
        getString(R.string.method),
        getString(R.string.status),
        getString(R.string.reference_number)
    };
    View view = inflater.inflate(R.layout.import_csv_data, container, false);
    mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);

    // use this setting to improve performance if you know that changes
    // in content do not change the layout size of the RecyclerView
    // http://www.vogella.com/tutorials/AndroidRecyclerView/article.html
    mRecyclerView.setHasFixedSize(true);

    // use a linear layout manager
    mLayoutManager = new LinearLayoutManager(getActivity());
    mRecyclerView.setLayoutManager(mLayoutManager);
    if (savedInstanceState!=null) {
      setData((ArrayList<CSVRecord>) savedInstanceState.getSerializable(KEY_DATASET));
    }

    return view;
  }

  public void setData(ArrayList<CSVRecord> data) {
    mDataset = data;
    int nrOfColumns = mDataset.get(0).size();
    ViewGroup.LayoutParams params=mRecyclerView.getLayoutParams();
    int dp = CELL_WIDTH*nrOfColumns+CHECKBOX_COLUMN_WIDTH+CELL_MARGIN*(nrOfColumns+2);
    params.width= (int) TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    mRecyclerView.setLayoutParams(params);
    mAdapter = new MyAdapter();
    mRecyclerView.setAdapter(mAdapter);
  }
  private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayAdapter<String> mFieldAdapter;

    private int nrOfColumns;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
      // each data item is just a string in this case
      public LinearLayout row;

      public ViewHolder(LinearLayout v) {
        super(v);
        row = v;
      }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter() {
      nrOfColumns = mDataset.get(0).size();
      mFieldAdapter = new ArrayAdapter<>(
          getActivity(),android.R.layout.simple_spinner_item,fields);
      mFieldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
      // create a new view
      LinearLayout v = new LinearLayout(parent.getContext());
      View cell;
      LinearLayout.LayoutParams params;
      params = new LinearLayout.LayoutParams(
          (int) TypedValue.applyDimension(
              TypedValue.COMPLEX_UNIT_DIP, CHECKBOX_COLUMN_WIDTH, getResources().getDisplayMetrics()),
          LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMargins(CELL_MARGIN, CELL_MARGIN, CELL_MARGIN, CELL_MARGIN);
      switch(viewType) {
        case 0:
          cell = new TextView(parent.getContext());
          break;
        default: {
          cell = new CheckBox(parent.getContext());
        }
      }
      v.addView(cell, params);
      params = new LinearLayout.LayoutParams(
          (int) TypedValue.applyDimension(
              TypedValue.COMPLEX_UNIT_DIP, CELL_WIDTH, getResources().getDisplayMetrics()),
          LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMargins(CELL_MARGIN, CELL_MARGIN, CELL_MARGIN, CELL_MARGIN);
      for (int i = 0; i < nrOfColumns; i++) {
        switch (viewType) {
          case 0:
            cell = new Spinner(parent.getContext());
            cell.setId(i);
            ((Spinner) cell).setAdapter(mFieldAdapter);
            ((Spinner) cell).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
              @Override
              public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                columnToFieldMap.put(parent.getId(),position);
              }

              @Override
              public void onNothingSelected(AdapterView<?> parent) {
                columnToFieldMap.put(parent.getId(),0);
              }
            });
            break;
          default:
            cell = new TextView(parent.getContext());
            ((TextView) cell).setSingleLine();
            ((TextView) cell).setEllipsize(TextUtils.TruncateAt.END);
            cell.setSelected(true);
        }
        v.addView(cell, params);
      }
      // set the view's size, margins, paddings and layout parameters
      ViewHolder vh = new ViewHolder(v);
      return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      // - get element from your dataset at this position
      // - replace the contents of the view with that element
      if (position>0) {
        final CSVRecord record = mDataset.get(position-1);
        for (int i = 0; i < nrOfColumns; i++) {
          ((TextView) holder.row.getChildAt(i+1)).setText(record.get(i));
        }
      } else {
        ((TextView) holder.row.getChildAt(0)).setText("Discard");
      }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
      return mDataset.size() +1;
    }

    @Override
    public int getItemViewType(int position) {
      return position==0 ? 0 : 1;
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putSerializable(KEY_DATASET, mDataset);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.cvs_import, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.IMPORT_COMMAND:
        for (Map.Entry<Integer, Integer> entry : columnToFieldMap.entrySet()) {
          Integer key = entry.getKey();
          Integer value = entry.getValue();
          Log.d("DEBUG",String.format("Column %d is mapped to field %d",key,value));
        }
        break;
    }
    return super.onOptionsItemSelected(item);
  }
}
