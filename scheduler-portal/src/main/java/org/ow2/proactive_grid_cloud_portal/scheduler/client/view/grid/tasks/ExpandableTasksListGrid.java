/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive_grid_cloud_portal.scheduler.client.view.grid.tasks;

import org.ow2.proactive_grid_cloud_portal.common.client.Settings;
import org.ow2.proactive_grid_cloud_portal.common.client.model.LogModel;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.Task;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.controller.TasksController;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.view.grid.GridColumns;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.DrawEvent;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.FieldStateChangedEvent;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.viewer.DetailViewer;
import com.smartgwt.client.widgets.viewer.DetailViewerField;
import com.smartgwt.client.widgets.viewer.DetailViewerRecord;


/**
 * A task list grid that has expand compoenent to show detail data.
 * @author the activeeon team.
 *
 */
public class ExpandableTasksListGrid extends TasksListGrid {

    //specifies the variable name of the task grid view state in the local storage
    private static final String TASKS_GRID_VIEW_STATE = "tasksGridViewState";

    /**
     * The record for the expand component to be shown.
     */
    private ListGridRecord expandRecord;

    /**
     * The columns and record factory used to build the expand component and the shown data.
     */
    protected ExpandTasksColumnsFactory expandTasksColumnsFactory;

    public ExpandableTasksListGrid(TasksController controller, ExpandableTasksColumnsFactory expandableFactory,
            ExpandTasksColumnsFactory expandFactory, String datasourceNamePrefix) {
        super(controller, expandableFactory, datasourceNamePrefix, false);
        this.expandTasksColumnsFactory = expandFactory;
        this.setCanExpandRecords(true);
    }

    protected DetailViewerField buildDetailViewer(GridColumns column) {
        return new DetailViewerField(column.getName(), column.getTitle());
    }

    @Override
    protected Canvas getExpansionComponent(final ListGridRecord record) {
        if (expandRecord != null && expandRecord != record) {
            this.collapseRecord(expandRecord);
        }
        this.expandRecord = record;

        Task t = TaskRecord.getTask(record);

        DetailViewer detail = new DetailViewer();
        detail.setWidth100();
        detail.setHeight100();
        detail.setCanSelectText(true);

        DetailViewerField[] fields = new DetailViewerField[4];
        fields[0] = buildDetailViewer(TasksColumnsFactory.HOST_ATTR);
        fields[1] = buildDetailViewer(TasksColumnsFactory.START_TIME_ATTR);
        fields[2] = buildDetailViewer(TasksColumnsFactory.FINISHED_TIME_ATTR);
        fields[3] = buildDetailViewer(TasksColumnsFactory.DESCRIPTION_ATTR);

        detail.setFields(fields);

        DetailViewerRecord detailRecord = new DetailViewerRecord();
        this.expandTasksColumnsFactory.buildRecord(t, detailRecord);
        detail.setData(new DetailViewerRecord[] { detailRecord });

        VLayout layout = new VLayout();
        layout.addMember(detail);

        return layout;
    }

    @Override
    public void tasksUpdating() {
        this.expandRecord = null;
    }

    @Override
    protected TaskRecord updateTaskRecord(Task task) {
        TaskRecord record = super.updateTaskRecord(task);
        String idAttr = TasksColumnsFactory.ID_ATTR.getName();
        if (this.expandRecord != null && record.getAttribute(idAttr).equals(this.expandRecord.getAttribute(idAttr))) {
            this.expandRecord = record;
        }
        return record;
    }

    @Override
    protected void fieldStateChangedHandler(FieldStateChangedEvent event) {
        //save the view state in the local storage
        Settings.get().setSetting(TASKS_GRID_VIEW_STATE, this.getViewState());
    }

    @Override
    protected void drawHandler(DrawEvent event) {
        try {
            String viewTaskState = Settings.get().getSetting(TASKS_GRID_VIEW_STATE);
            if (viewTaskState != null) {
                // restore any previously saved view state for this grid
                this.setViewState(viewTaskState);
            }
        } catch (Exception e) {
            LogModel.getInstance().logImportantMessage("Failed to restore tasks grid view state " + e);
        }

    }
}
