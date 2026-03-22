package com.example.signin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    private final List<InboxItem> inboxList;
    private final Context context;

    public InboxAdapter(Context context, List<InboxItem> inboxList) {
        this.context = context;
        this.inboxList = inboxList;
        sortDescending();
    }

    public void addInboxItem(InboxItem item) {
        if (item == null) return;
        inboxList.add(0, item);
        sortDescending();
        notifyDataSetChanged();
    }

    public void clear() {
        inboxList.clear();
        notifyDataSetChanged();
    }

    private void sortDescending() {
        Collections.sort(inboxList, (a, b) -> Long.compare(b.getTimestampMillis(), a.getTimestampMillis()));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inbox, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InboxItem item = inboxList.get(position);

        // Support both: (1) Firestore arrival/queue notifications, (2) file attachments
        boolean isNotification = (item.getEstName() != null || item.getEstId() != null)
                || "arrival_update".equals(item.getType()) || "appointment".equals(item.getType()) || "info".equals(item.getType());

        if (isNotification) {
            String title = item.getEstName() != null ? item.getEstName() : "Notification";
            if (item.getCounterName() != null && !item.getCounterName().isEmpty()) {
                title += " — " + item.getCounterName();
            }
            holder.tvSender.setText(title);
            holder.tvFileName.setText(item.getStatus() != null ? item.getStatus() : item.getType() != null ? item.getType() : "");
        } else {
            holder.tvSender.setText(item.getSender() != null ? item.getSender() : "Unknown Sender");
            holder.tvFileName.setText(item.getFileName() != null ? item.getFileName() : "No File");
        }

        String message = item.getMessage() != null ? item.getMessage() : "";
        if (item.getSize() > 0) {
            double sizeMb = item.getSize() / (1024.0 * 1024.0);
            message += String.format(Locale.getDefault(), " • %.2f MB", sizeMb);
        }
        holder.tvMessage.setText(message);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
        holder.tvTimestamp.setText(sdf.format(new Date(item.getTimestampMillis())));

        holder.itemView.setOnClickListener(v -> {
            // Notifications: show message (redirectUrl is for web app; mobile shows content)
            if (isNotification) {
                Toast.makeText(context, item.getMessage() != null ? item.getMessage() : "Notification", Toast.LENGTH_LONG).show();
                return;
            }
            if (item.getFileUrl() != null && !item.getFileUrl().isEmpty()) {
                try {
                    if (item.getFileUrl().startsWith("data:")) {
                        String data = item.getFileUrl();
                        int semi = data.indexOf(';');
                        int comma = data.indexOf(',');
                        String mime = "application/octet-stream";
                        if (semi > 5) {
                            mime = data.substring(5, semi);
                        }
                        if (comma > 0) {
                            String payload = data.substring(comma + 1);
                            byte[] bytes = android.util.Base64.decode(payload, android.util.Base64.DEFAULT);
                            if (mime.startsWith("image/")) {
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                ImageView imageView = new ImageView(context);
                                imageView.setImageBitmap(bmp);
                                new AlertDialog.Builder(context)
                                        .setTitle(item.getFileName() != null ? item.getFileName() : "Image")
                                        .setView(imageView)
                                        .setPositiveButton("Close", (d, w) -> d.dismiss())
                                        .show();
                            } else {
                                String name = item.getFileName() != null ? item.getFileName() : ("document_" + System.currentTimeMillis());
                                File outFile = new File(context.getCacheDir(), name);
                                try (OutputStream os = new FileOutputStream(outFile)) {
                                    os.write(bytes);
                                }
                                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", outFile);
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(uri, mime);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                context.startActivity(intent);
                            }
                        } else {
                            Toast.makeText(context, "Invalid data", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(item.getFileUrl()));
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "Cannot open file URL", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "No file URL attached", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return inboxList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvFileName, tvMessage, tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
