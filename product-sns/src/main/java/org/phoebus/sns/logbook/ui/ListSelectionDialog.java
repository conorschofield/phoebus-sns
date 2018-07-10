/*******************************************************************************
 * Copyright (c) 2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.phoebus.sns.logbook.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.phoebus.framework.preferences.PhoebusPreferenceService;
import org.phoebus.ui.dialog.DialogHelper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
/**
 * @author Evan Smith
 */
public class ListSelectionDialog extends Dialog<Boolean>
{
    private final HBox content;
    
    @SuppressWarnings("unused") // Used to initialize list views.
    private final Supplier<ObservableList<String>> available;
    @SuppressWarnings("unused") // Used to initialize list views.
    private final Supplier<ObservableList<String>> selected;
    
    private final Function<String, Boolean> addSelected, removeSelected;
    
    private final Font labelFont;
    private final VBox selectedBox, buttonsBox, availableBox;
    private final Label selectedLabel, itemsLabel;
    private final ListView<String> availableItems;
    private final ListView<String> selectedItems;

    private final int buttonWidth = 75, spacing = 10;
    private final Button add, remove, clear;

    public ListSelectionDialog(Node root,
                              String title,
                              Supplier<ObservableList<String>>    available, 
                              Supplier<ObservableList<String>>    selected,
                              Function<String, Boolean> addSelected,
                              Function<String, Boolean> removeSelected)
    {   
        this.available      = available;
        this.selected       = selected;
        this.addSelected    = addSelected;
        this.removeSelected = removeSelected;
        
        content      = new HBox();
        selectedBox  = new VBox();
        buttonsBox   = new VBox();
        availableBox = new VBox();
        
        add    = new Button("Add");
        remove = new Button("Remove");
        clear  = new Button("Clear");
        
        labelFont     = new Font(16);
        selectedLabel = new Label("Selected");
        itemsLabel    = new Label("Available");
        
        selectedItems  = new ListView<String>(selected.get());
        // We wan't to remove items from the available list as they're selected, and add them back as they are unselected. 
        // Due to this we need a copy as available.get() returns an immutable list.
        availableItems = new ListView<String>(
                FXCollections.observableArrayList(List.copyOf(available.get())));
        
        for (String item : selectedItems.getItems())
            availableItems.getItems().remove(item);
        
        setTitle(title);
        
        formatContent();
        
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);
        getDialogPane().setContent(content);
        
        setResizable(true);
        
        DialogHelper.positionAndSize(this, root,
                PhoebusPreferenceService.userNodeForClass(ListSelectionDialog.class),
                400, 600);
        
        setResultConverter(button ->
        {
            return button == ButtonType.APPLY;
        });
    }

    private void formatContent()
    {
        selectedItems.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        availableItems.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        selectedItems.setStyle("-fx-control-inner-background-alt: white");
        availableItems.setStyle("-fx-control-inner-background-alt: white");

        add.setOnAction(event ->
        {
            // Can't modify list we're iterating over, so make a copy to iterate over.
            List<String> toAdd = new ArrayList<String>(availableItems.getSelectionModel().getSelectedItems());
            for (String item : toAdd)
            {
                addSelected.apply(item);
                availableItems.getItems().remove(item);
            }
            clearSelections();
        });
        
        remove.setOnAction(event ->
        {
            // Can't modify list we're iterating over, so make a copy to iterate over.
            List<String> toRemove = new ArrayList<String>(selectedItems.getSelectionModel().getSelectedItems());
            for (String item : toRemove)
            {
                removeSelected.apply(item);
                availableItems.getItems().add(item);
            }
            Collections.sort(availableItems.getItems());
            clearSelections();
        });
        
        clear.setOnAction(event ->
        {
            // Can't modify list we're iterating over, so make a copy to iterate over.
            List<String> toRemove = new ArrayList<String>(selectedItems.getItems());
            for (String item : toRemove)
            {
                removeSelected.apply(item);
                availableItems.getItems().add(item);
            }
            Collections.sort(availableItems.getItems());
            clearSelections();
        });

        content.setAlignment(Pos.CENTER);
        content.setSpacing(spacing);
        
        add.setPrefWidth(buttonWidth);
        remove.setPrefWidth(buttonWidth);
        clear.setPrefWidth(buttonWidth);
        
        buttonsBox.setSpacing(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.getChildren().addAll(add, remove, clear);

        itemsLabel.setFont(labelFont);
        VBox.setVgrow(availableItems, Priority.ALWAYS);
        availableBox.setSpacing(spacing);
        availableBox.getChildren().addAll(itemsLabel, availableItems);
        
        selectedLabel.setFont(labelFont);
        VBox.setVgrow(selectedItems, Priority.ALWAYS);
        selectedBox.setSpacing(spacing);
        
        selectedBox.getChildren().addAll(selectedLabel, selectedItems);

        HBox.setMargin(availableBox, new Insets(5,  0, 10, 10));
        HBox.setMargin(buttonsBox,   new Insets(5,  0, 10,  0));
        HBox.setMargin(selectedBox,  new Insets(5, 10, 10,  0));
        
        content.getChildren().addAll(availableBox, buttonsBox, selectedBox);
    }

    private void clearSelections()
    {
        selectedItems.getSelectionModel().clearSelection();
        availableItems.getSelectionModel().clearSelection();
    }
}