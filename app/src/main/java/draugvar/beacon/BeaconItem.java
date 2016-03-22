package draugvar.beacon;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;

import org.altbeacon.beacon.Beacon;

public class BeaconItem extends AbstractItem<BeaconItem, BeaconItem.ViewHolder> {
    public String name;
    public String description;


    //The unique ID for this type of item
    @Override
    public int getType() {
        return R.id.beacon_item;
    }

    //The layout to be used for this type of item
    @Override
    public int getLayoutRes() {
        return R.layout.beacon_layout;
    }

    //The logic to bind your data to the view
    @Override
    public void bindView(ViewHolder viewHolder) {
        //call super so the selection is already handled for you
        super.bindView(viewHolder);

        //bind our data
        //set the text for the name
        viewHolder.name.setText(name);
        //set the text for the description or hide
        viewHolder.description.setText(description);
    }

    //The viewHolder used for this item. This viewHolder is always reused by the RecyclerView so scrolling is blazing fast
    protected static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected TextView description;

        public ViewHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.id1);
            this.description = (TextView) view.findViewById(R.id.potenza);
        }
    }
}