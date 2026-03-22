package com.example.signin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter for displaying a list of establishments in a ListView.
 */
public class EstablishmentAdapter extends ArrayAdapter<EstablishmentItem> {

    public EstablishmentAdapter(Context context, List<EstablishmentItem> items) {
        super(context, 0, items);
    }

    /**
     * Updates the adapter with a new list of items.
     *
     * @param newItems List of EstablishmentItem
     */
    public void updateList(List<EstablishmentItem> newItems) {
        clear();
        addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate item layout if not already created
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_establishment, parent, false);
        }

        EstablishmentItem item = getItem(position);

        // Bind views
        TextView tvName = convertView.findViewById(R.id.tvEstName);
        TextView tvHours = convertView.findViewById(R.id.tvEstHours);
        TextView tvContact = convertView.findViewById(R.id.tvEstContact);
        TextView tvEmail = convertView.findViewById(R.id.tvEstEmail);
        TextView tvPerson = convertView.findViewById(R.id.tvEstPerson);
        TextView tvDepartments = convertView.findViewById(R.id.tvEstDepartments);
        TextView tvServices = convertView.findViewById(R.id.tvEstServices);

        if (item != null) {
            tvName.setText(item.getName() != null ? item.getName() : "N/A");
            tvHours.setText("Hours: " + (item.getHours() != null ? item.getHours() : "N/A"));
            tvContact.setText("Contact: " + (item.getContactInfo() != null ? item.getContactInfo() : "N/A"));
            tvEmail.setText("Email: " + (item.getEmail() != null ? item.getEmail() : "N/A"));
            tvPerson.setText("Contact Person: " + (item.getContactPerson() != null ? item.getContactPerson() : "N/A"));

            tvDepartments.setText("Departments: " +
                    (item.getDepartments() != null && !item.getDepartments().isEmpty() ?
                            String.join(", ", item.getDepartments()) : "N/A"));

            tvServices.setText("Services: " +
                    (item.getServices() != null && !item.getServices().isEmpty() ?
                            String.join(", ", item.getServices()) : "N/A"));
        }

        return convertView;
    }
}
