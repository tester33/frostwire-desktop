/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.limegroup.gnutella.gui.search;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.limewire.setting.BooleanSetting;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledTextField;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
final class SearchOptionsPanel extends JPanel {

    private final SearchResultMediator resultPanel;

    private final LabeledTextField textFieldKeywords;
    private final LabeledRangeSlider sliderSize;
    private final LabeledRangeSlider sliderSeeds;
    private final Map<SearchEngine,JCheckBox> engineCheckboxes;
    
    private GeneralResultFilter generalFilter;
    
    

    public SearchOptionsPanel(SearchResultMediator resultPanel) {
        this.resultPanel = resultPanel;

        engineCheckboxes = new HashMap<>();
        
        setLayout(new MigLayout("insets 0, fillx"));

        add(createSearchEnginesFilter(), "wrap");
        
        add(new JSeparator(SwingConstants.HORIZONTAL), "growx, wrap");

        this.textFieldKeywords = createNameFilter();
        add(textFieldKeywords, "gapx 3, wrap");
        
        add(new JSeparator(SwingConstants.HORIZONTAL), "growx, wrap");

        this.sliderSize = createSizeFilter();
        add(sliderSize, "gapx 3, wrap");
        
        add(new JSeparator(SwingConstants.HORIZONTAL), "growx, wrap");

        this.sliderSeeds = createSeedsFilter();
        add(sliderSeeds, "gapx 3, wrap");
        
        add(new JSeparator(SwingConstants.HORIZONTAL), "growx, wrap");

        resetFilters();
    }

    public void updateFiltersPanel() {
        generalFilter = new GeneralResultFilter(resultPanel, sliderSeeds, sliderSize, textFieldKeywords);
        resultPanel.filterChanged(new SearchEngineFilter(engineCheckboxes), 0);
        resultPanel.filterChanged(generalFilter, 1);
    }

    public void resetFilters() {
        sliderSeeds.setMinimum(0);
        sliderSeeds.setMaximum(1000);
        sliderSeeds.setLowerValue(0);
        sliderSeeds.setUpperValue(1000);

        sliderSize.setMinimum(0);
        sliderSize.setMaximum(1000);
        sliderSize.setLowerValue(0);
        sliderSize.setUpperValue(1000);

        sliderSeeds.getMinimumValueLabel().setText(I18n.tr("0"));
        sliderSeeds.getMaximumValueLabel().setText(I18n.tr("Max"));
        sliderSize.getMinimumValueLabel().setText(I18n.tr("0"));
        sliderSize.getMaximumValueLabel().setText(I18n.tr("Max"));

        textFieldKeywords.setText("");
    }

    private JComponent createSearchEnginesFilter() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("insets 3, wrap 2"));
        List<SearchEngine> searchEngines = SearchEngine.getEngines();
        setupCheckboxes(searchEngines, panel);

        //panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeMediator.LIGHT_BORDER_COLOR));

        return panel;
    }

    private LabeledTextField createNameFilter() {
        LabeledTextField textField = new LabeledTextField(I18n.tr("Name"), 40, -1, 200);
        
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                textFieldKeywords_keyReleased(e);
            }
        });

        return textField;
    }

    private LabeledRangeSlider createSizeFilter() {
        LabeledRangeSlider slider = new LabeledRangeSlider(I18n.tr("Size"), null, 0, 1000);
        slider.setPreferredSize(new Dimension(200, (int) slider.getPreferredSize().getHeight()));
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                sliderSize_stateChanged(e);
            }
        });

        return slider;
    }

    private LabeledRangeSlider createSeedsFilter() {
        LabeledRangeSlider slider = new LabeledRangeSlider(I18n.tr("Seeds"), null, 0, 1000);
        slider.setPreferredSize(new Dimension(200, (int) slider.getPreferredSize().getHeight()));
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                sliderSeeds_stateChanged(e);
            }
        });

        return slider;
    }

    private void setupCheckboxes(List<SearchEngine> searchEngines, JPanel parent) {

        final Map<JCheckBox, BooleanSetting> cBoxes = new HashMap<JCheckBox, BooleanSetting>();

        ItemListener listener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                boolean allDeSelected = true;

                for (JCheckBox cBox : cBoxes.keySet()) {
                    if (cBox.isSelected()) {
                        allDeSelected = false;
                        break;
                    }
                }

                if (allDeSelected) {
                    ((JCheckBox) e.getItemSelectable()).setSelected(true);
                }

//                for (JCheckBox cBox : cBoxes.keySet()) {
//                    cBoxes.get(cBox).setValue(cBox.isSelected());
//                }

                if (resultPanel != null) {
                    resultPanel.filterChanged(new SearchEngineFilter(engineCheckboxes), 0);
                }
            }
        };

        for (SearchEngine se : searchEngines) {
            JCheckBox cBox = new JCheckBox(se.getName());
            cBox.setSelected(se.isEnabled());
            cBox.setEnabled(se.isEnabled());
            
            if (!cBox.isEnabled()) {
                cBox.setToolTipText(se.getName() + " " + I18n.tr("has been disabled on your FrostWire Search Options. (Go to Tools > Options > Search to enable)"));
            }
            
            parent.add(cBox);

            cBoxes.put(cBox, se.getEnabledSetting());
            cBox.addItemListener(listener);
            
            engineCheckboxes.put(se,cBox);
        }
    }

    private void textFieldKeywords_keyReleased(KeyEvent e) {
        if (generalFilter != null) {
            generalFilter.updateKeywordFiltering(textFieldKeywords.getText());
        }
    }

    private void sliderSize_stateChanged(ChangeEvent e) {
        if (generalFilter != null) {
            generalFilter.setRangeSize(sliderSize.getLowerValue(), sliderSize.getUpperValue());
        }
    }

    private void sliderSeeds_stateChanged(ChangeEvent e) {
        //System.out.println(sliderSeeds.getLowerValue() + " - " + sliderSeeds.getUpperValue());
        if (generalFilter != null) {
            generalFilter.setRangeSeeds(sliderSeeds.getLowerValue(), sliderSeeds.getUpperValue());
        }
    }
}
