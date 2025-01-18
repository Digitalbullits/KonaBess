
        }
    }

    private static void offset_initial_level(int bin_id, int offset) throws Exception {
        if (ChipInfo.which == ChipInfo.type.kona_singleBin
                || ChipInfo.which == ChipInfo.type.msmnile_singleBin
                || ChipInfo.which == ChipInfo.type.lahaina_singleBin
                || ChipInfo.which == ChipInfo.type.waipio_singleBin
                || ChipInfo.which == ChipInfo.type.cape_singleBin
                || ChipInfo.which == ChipInfo.type.ukee_singleBin
                || ChipInfo.which == ChipInfo.type.cliffs_singleBin
                || ChipInfo.which == ChipInfo.type.cliffs_7_singleBin
                || ChipInfo.which == ChipInfo.type.kalama_sg_singleBin) {
            offset_initial_level_old(offset);
            return;
        }
        for (int i = 0; i < bins.get(bin_id).header.size(); i++) {
            String line = bins.get(bin_id).header.get(i);
            if (line.contains("qcom,initial-pwrlevel")) {
                bins.get(bin_id).header.set(i,
                        DtsHelper.encodeIntOrHexLine(
                                DtsHelper.decode_int_line(line).name,
                                DtsHelper.decode_int_line(line).value + offset + ""));
                break;
            }
        }
    }

    private static void offset_ca_target_level(int bin_id, int offset) throws Exception {
        for (int i = 0; i < bins.get(bin_id).header.size(); i++) {
            String line = bins.get(bin_id).header.get(i);
            if (line.contains("qcom,ca-target-pwrlevel")) {
                bins.get(bin_id).header.set(i,
                        DtsHelper.encodeIntOrHexLine(
                                DtsHelper.decode_int_line(line).name,
                                DtsHelper.decode_int_line(line).value + offset + ""));
                break;
            }
        }
    }

    private static void patch_throttle_level_old() throws Exception {
        boolean started = false;
        int bracket = 0;
        for (int i = 0; i < lines_in_dts.size(); i++) {
            String line = lines_in_dts.get(i);

            if (line.contains("qcom,kgsl-3d0") && line.contains("{")) {
                started = true;
                bracket++;
                continue;
            }

            if (line.contains("{")) {
                bracket++;
                continue;
            }

            if (line.contains("}")) {
                bracket--;
                if (bracket == 0)
                    break;
                continue;
            }

            if (!started)
                continue;

            if (line.contains("qcom,throttle-pwrlevel")) {
                lines_in_dts.set(i,
                        DtsHelper.encodeIntOrHexLine(DtsHelper.decode_int_line(line).name,
                                "0"));
            }

        }
    }

    private static void patch_throttle_level() throws Exception {
        if (ChipInfo.which == ChipInfo.type.kona_singleBin
                || ChipInfo.which == ChipInfo.type.msmnile_singleBin
                || ChipInfo.which == ChipInfo.type.lahaina_singleBin
                || ChipInfo.which == ChipInfo.type.waipio_singleBin
                || ChipInfo.which == ChipInfo.type.cape_singleBin
                || ChipInfo.which == ChipInfo.type.ukee_singleBin
                || ChipInfo.which == ChipInfo.type.cliffs_singleBin
                || ChipInfo.which == ChipInfo.type.cliffs_7_singleBin
                || ChipInfo.which == ChipInfo.type.kalama_sg_singleBin) {
            patch_throttle_level_old();
            return;
        }
        for (int bin_id = 0; bin_id < bins.size(); bin_id++) {
            for (int i = 0; i < bins.get(bin_id).header.size(); i++) {
                String line = bins.get(bin_id).header.get(i);
                if (line.contains("qcom,throttle-pwrlevel")) {
                    bins.get(bin_id).header.set(i,
                            DtsHelper.encodeIntOrHexLine(
                                    DtsHelper.decode_int_line(line).name, "0"));
                    break;
                }
            }
        }
    }

    public static boolean canAddNewLevel(int binID, Context context) throws Exception {
        int max_levels = ChipInfo.getMaxTableLevels(ChipInfo.which) - min_level_chip_offset();
        if (bins.get(binID).levels.size() <= max_levels)
            return true;
        Toast.makeText(context, R.string.unable_add_more, Toast.LENGTH_SHORT).show();
        return false;
    }

    public static int min_level_chip_offset() throws Exception {
        if (ChipInfo.which == ChipInfo.type.lahaina || ChipInfo.which == ChipInfo.type.lahaina_singleBin
                || ChipInfo.which == ChipInfo.type.shima || ChipInfo.which == ChipInfo.type.yupik
                || ChipInfo.which == ChipInfo.type.waipio_singleBin
                || ChipInfo.which == ChipInfo.type.cape_singleBin
                || ChipInfo.which == ChipInfo.type.kalama
                || ChipInfo.which == ChipInfo.type.diwali
                || ChipInfo.which == ChipInfo.type.ukee_singleBin
                || ChipInfo.which == ChipInfo.type.pineapple
                || ChipInfo.which == ChipInfo.type.cliffs_singleBin
                || ChipInfo.which == ChipInfo.type.cliffs_7_singleBin
                || ChipInfo.which == ChipInfo.type.kalama_sg_singleBin
                || ChipInfo.which == ChipInfo.type.sun)
            return 1;
        if (ChipInfo.which == ChipInfo.type.kona || ChipInfo.which == ChipInfo.type.kona_singleBin
                || ChipInfo.which == ChipInfo.type.msmnile || ChipInfo.which == ChipInfo.type.msmnile_singleBin
                || ChipInfo.which == ChipInfo.type.lito_v1 || ChipInfo.which == ChipInfo.type.lito_v2
                || ChipInfo.which == ChipInfo.type.lagoon)
            return 2;
        throw new Exception();
    }

    private static void generateLevels(Activity activity, int id, LinearLayout page) throws Exception {
        ((MainActivity) activity).onBackPressedListener = new MainActivity.onBackPressedListener() {
            @Override
            public void onBackPressed() {
                try {
                    generateBins(activity, page);
                } catch (Exception ignored) {
                }
            }
        };

        ListView listView = new ListView(activity);
        ArrayList<ParamAdapter.item> items = new ArrayList<>();

        items.add(new ParamAdapter.item() {{
            title = activity.getResources().getString(R.string.back);
            subtitle = "";
        }});

        items.add(new ParamAdapter.item() {{
            title = activity.getResources().getString(R.string.new_item);
            subtitle = activity.getResources().getString(R.string.new_desc);
        }});

        for (level level : bins.get(id).levels) {
            long freq = getFrequencyFromLevel(level);
            if (freq == 0)
                continue;
            ;
            ParamAdapter.item item = new ParamAdapter.item();
            item.title = freq / 1000000 + "MHz";
            item.subtitle = "";
            items.add(item);
        }

        items.add(new ParamAdapter.item() {{
            title = activity.getResources().getString(R.string.new_item);
            subtitle = activity.getResources().getString(R.string.new_desc);
        }});

        listView.setOnItemClickListener((parent, view, position, id1) -> {
            if (position == items.size() - 1) {
                try {
                    if (!canAddNewLevel(id, activity))
                        return;
                    bins.get(id).levels.add(bins.get(id).levels.size() - min_level_chip_offset(),
                            level_clone(bins.get(id).levels.get(bins.get(id).levels.size() - min_level_chip_offset())));
                    generateLevels(activity, id, page);
                    offset_initial_level(id, 1);
                    if (ChipInfo.which == ChipInfo.type.lito_v1 || ChipInfo.which == ChipInfo.type.lito_v2 || ChipInfo.which == ChipInfo.type.lagoon)
                        offset_ca_target_level(id, 1);
                } catch (Exception e) {
                    DialogUtil.showError(activity, R.string.error_occur);
                }
                return;
            }
            if (position == 0) {
                try {
                    generateBins(activity, page);
                } catch (Exception ignored) {
                }
                return;
            }
            if (position == 1) {
                try {
                    if (!canAddNewLevel(id, activity))
                        return;
                    bins.get(id).levels.add(0, level_clone(bins.get(id).levels.get(0)));
                    generateLevels(activity, id, page);
                    offset_initial_level(id, 1);
                    if (ChipInfo.which == ChipInfo.type.lito_v1 || ChipInfo.which == ChipInfo.type.lito_v2 || ChipInfo.which == ChipInfo.type.lagoon)
                        offset_ca_target_level(id, 1);
                } catch (Exception e) {
                    DialogUtil.showError(activity, R.string.error_occur);
                }
                return;
            }
            position -= 2;
            try {
                generateALevel(activity, id, position, page);
            } catch (Exception e) {
                DialogUtil.showError(activity, R.string.error_occur);
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, idd) -> {
            if (position == items.size() - 1)
                return true;
            if (bins.get(id).levels.size() == 1)
                return true;
            try {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.remove)
                        .setMessage(String.format(activity.getResources().getString(R.string.remove_msg),
                                getFrequencyFromLevel(bins.get(id).levels.get(position - 2)) / 1000000))
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            bins.get(id).levels.remove(position - 2);
                            try {
                                generateLevels(activity, id, page);
                                offset_initial_level(id, -1);
                                if (ChipInfo.which == ChipInfo.type.lito_v1 || ChipInfo.which == ChipInfo.type.lito_v2 || ChipInfo.which == ChipInfo.type.lagoon)
                                    offset_ca_target_level(id, -1);
                            } catch (Exception e) {
                                DialogUtil.showError(activity, R.string.error_occur);
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .create().show();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            return true;
        });

        listView.setAdapter(new ParamAdapter(items, activity));

        page.removeAllViews();
        page.addView(listView);
    }

    private static long getFrequencyFromLevel(level level) throws Exception {
        for (String line : level.lines) {
            if (line.contains("qcom,gpu-freq")) {
                return DtsHelper.decode_int_line(line).value;
            }
        }
        throw new Exception();
    }

    private static void generateBins(Activity activity, LinearLayout page) throws Exception {
        ((MainActivity) activity).onBackPressedListener = new MainActivity.onBackPressedListener() {
            @Override
            public void onBackPressed() {
                ((MainActivity) activity).showMainView();
            }
        };

        ListView listView = new ListView(activity);
        ArrayList<ParamAdapter.item> items = new ArrayList<>();

        for (int i = 0; i < bins.size(); i++) {
            ParamAdapter.item item = new ParamAdapter.item();
            item.title = KonaBessStr.convert_bins(bins.get(i).id, activity);
            item.subtitle = "";
            items.add(item);
        }

        listView.setAdapter(new ParamAdapter(items, activity));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                generateLevels(activity, position, page);
            } catch (Exception e) {
                DialogUtil.showError(activity, R.string.error_occur);
            }
        });

        page.removeAllViews();
        page.addView(listView);
    }

    private static View generateToolBar(Activity activity) {
        LinearLayout toolbar = new LinearLayout(activity);
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(activity);
        horizontalScrollView.addView(toolbar);

        {
            Button button = new Button(activity);
            button.setText(R.string.save_freq_table);
            toolbar.addView(button);
            button.setOnClickListener(v -> {
                try {
                    writeOut(genBack(genTable()));
                    Toast.makeText(activity, R.string.save_success, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    DialogUtil.showError(activity, R.string.save_failed);
                }
            });
        }
        return horizontalScrollView;
    }

    static class gpuTableLogic extends Thread {
        Activity activity;
        AlertDialog waiting;
        LinearLayout showedView;
        LinearLayout page;

        public gpuTableLogic(Activity activity, LinearLayout showedView) {
            this.activity = activity;
            this.showedView = showedView;
        }

        public void run() {
            activity.runOnUiThread(() -> {
                waiting = DialogUtil.getWaitDialog(activity, R.string.getting_freq_table);
                waiting.show();
            });

            try {
                init();
                decode();
                patch_throttle_level();
            } catch (Exception e) {
                activity.runOnUiThread(() -> DialogUtil.showError(activity,
                        R.string.getting_freq_table_failed));
            }

            activity.runOnUiThread(() -> {
                waiting.dismiss();
                showedView.removeAllViews();
                showedView.addView(generateToolBar(activity));
                page = new LinearLayout(activity);
                page.setOrientation(LinearLayout.VERTICAL);
                try {
                    generateBins(activity, page);
                } catch (Exception e) {
                    DialogUtil.showError(activity, R.string.getting_freq_table_failed);
                }
                showedView.addView(page);
            });

        }
    }
                                                  }
