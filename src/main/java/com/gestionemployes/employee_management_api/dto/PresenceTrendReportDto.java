package com.gestionemployes.employee_management_api.dto;

import java.util.Map;

public class PresenceTrendReportDto {
	// Total d'heures travaillées par jour de la semaine 
	private Map<String, String> totalHoursByDayOfWeek;
	// Total d'heures travaillées par mois (toutes années confondues)
	private Map<Integer, String> totalHoursByMonth;
	// Total d'heures travaillées par mois et année 
	private Map<String, String> totalHoursByMonthYear;
	// Heures moyennes travaillées par jour (sur tous les employés et toutes les dates)
	private String averageDailyHoursAcrossAllEmployees;
	// Total d'heures travaillées par employé (ID de l'employé -> heures formatées)
	private Map<Long, String> totalHoursByEmployeeId;
	// Total d'heures travaillées par nom de département (Nom du département ->
	// heures formatées)
	private Map<String, String> totalHoursByDepartementName;

	public PresenceTrendReportDto() {
	}

	public PresenceTrendReportDto(Map<String, String> totalHoursByDayOfWeek, Map<Integer, String> totalHoursByMonth,
			Map<String, String> totalHoursByMonthYear, String averageDailyHoursAcrossAllEmployees,
			Map<Long, String> totalHoursByEmployeeId, Map<String, String> totalHoursByDepartementName) {
		this.totalHoursByDayOfWeek = totalHoursByDayOfWeek;
		this.totalHoursByMonth = totalHoursByMonth;
		this.totalHoursByMonthYear = totalHoursByMonthYear;
		this.averageDailyHoursAcrossAllEmployees = averageDailyHoursAcrossAllEmployees;
		this.totalHoursByEmployeeId = totalHoursByEmployeeId;
		this.totalHoursByDepartementName = totalHoursByDepartementName;
	}

	// --- Getters et Setters ---
	public Map<String, String> getTotalHoursByDayOfWeek() {
		return totalHoursByDayOfWeek;
	}

	public void setTotalHoursByDayOfWeek(Map<String, String> totalHoursByDayOfWeek) {
		this.totalHoursByDayOfWeek = totalHoursByDayOfWeek;
	}

	public Map<Integer, String> getTotalHoursByMonth() {
		return totalHoursByMonth;
	}

	public void setTotalHoursByMonth(Map<Integer, String> totalHoursByMonth) {
		this.totalHoursByMonth = totalHoursByMonth;
	}

	public Map<String, String> getTotalHoursByMonthYear() {
		return totalHoursByMonthYear;
	}

	public void setTotalHoursByMonthYear(Map<String, String> totalHoursByMonthYear) {
		this.totalHoursByMonthYear = totalHoursByMonthYear;
	}

	public String getAverageDailyHoursAcrossAllEmployees() {
		return averageDailyHoursAcrossAllEmployees;
	}

	public void setAverageDailyHoursAcrossAllEmployees(String averageDailyHoursAcrossAllEmployees) {
		this.averageDailyHoursAcrossAllEmployees = averageDailyHoursAcrossAllEmployees;
	}

	public Map<Long, String> getTotalHoursByEmployeeId() {
		return totalHoursByEmployeeId;
	}

	public void setTotalHoursByEmployeeId(Map<Long, String> totalHoursByEmployeeId) {
		this.totalHoursByEmployeeId = totalHoursByEmployeeId;
	}

	public Map<String, String> getTotalHoursByDepartementName() {
		return totalHoursByDepartementName;
	}

	public void setTotalHoursByDepartementName(Map<String, String> totalHoursByDepartementName) {
		this.totalHoursByDepartementName = totalHoursByDepartementName;
	}
}
