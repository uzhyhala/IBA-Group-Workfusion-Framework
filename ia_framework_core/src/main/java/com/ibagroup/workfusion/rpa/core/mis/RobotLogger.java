package com.ibagroup.workfusion.rpa.core.mis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import groovy.lang.Binding;
import com.ibagroup.workfusion.rpa.core.datastore.DataStoreInsert;
import com.ibagroup.workfusion.rpa.core.robots.factory.RunnerContext;

public class RobotLogger implements IRobotLogger {

	static final Logger logger = LoggerFactory.getLogger(RobotLogger.class);
	public final String campaignUuid; 

	private Binding binding;
	private String actionsDSname;
	private String detailsDSname;

	List<TaskDetail> taskDetails = new ArrayList<>();
	List<TaskAction> taskActions = new ArrayList<>();

	public RobotLogger(Binding binding, String actionsDSname, String detailsDSname) {
		this.binding = binding;
		this.campaignUuid = RunnerContext.getCampaignUuid();
		this.actionsDSname = actionsDSname;
		this.detailsDSname = detailsDSname;
	}

	@Override
	public boolean storeLogs() {
		logger.info("logging actions " + this.getActions().size() + " actions");

		boolean allSuccess = true;
		DataStoreInsert dsAccess = new DataStoreInsert(binding);

		List<TaskAction> taskActionList = new ArrayList<TaskAction>(getActions());

		while (!taskActionList.isEmpty()) {
			if (taskActionList.get(0).getTransactional()) {
				List<TaskAction> temp = new ArrayList<>();
				for (TaskAction action : taskActionList) {
					if (action.getOperation().equals(taskActionList.get(0).getOperation())
							&& action.getRecordUuid().equals(taskActionList.get(0).getRecordUuid())) {
						temp.add(action);
					}
				}
				taskActionList.removeAll(temp);	

				Long minStartTime = temp.stream().mapToLong(el -> el.getStartTime()).min().getAsLong();
				Long maxEndTime = temp.stream().mapToLong(el -> el.getEndTime()).max().getAsLong();

				dsAccess.insertRow(actionsDSname,
						new TaskAction(
								temp.stream().filter(el -> el.getStartTime().equals(minStartTime)).findFirst().get(),
								temp.stream().filter(el -> el.getEndTime().equals(maxEndTime)).findFirst().get())
										.getMap());
			} else {
				dsAccess.insertRow(actionsDSname, taskActionList.get(0).getMap());
				taskActionList.remove(0);
			}
		}

		for (TaskDetail detail : getDetails()) {
			dsAccess.insertRow(detailsDSname, detail.getMap());
		}
		clearActions();
		clearDetails();

		return allSuccess;
	}

	@Override
	public void addAction(TaskAction... actions) {
		this.taskActions.addAll(java.util.Arrays.asList(actions));
	}

	@Override
	public List<TaskAction> getActions() {
		return Collections.unmodifiableList(taskActions);
	}

	@Override
	public void clearActions() {
		taskActions.clear();
	}

	@Override
	public void addDetails(TaskDetail... details) {
		this.taskDetails.addAll(java.util.Arrays.asList(details));
	}

	@Override
	public List<TaskDetail> getDetails() {
		return Collections.unmodifiableList(taskDetails);
	}

	@Override
	public void clearDetails() {
		taskDetails.clear();
	}

}
