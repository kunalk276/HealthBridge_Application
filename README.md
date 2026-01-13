# HealthBridge – Multilingual AI Health Assistant

HealthBridge is a multilingual AI health assistant that explains symptoms clearly, evaluates severity, and guides users on what to do next. It also includes an admin dashboard for public-health monitoring.

## Overview
HealthBridge helps users describe symptoms in their own language, get an AI-generated explanation, understand severity levels, and receive hospital alerts when needed. The system supports both individual users and administrators who monitor broader health trends.

## Key Features
- Symptom analysis in multiple Indian languages
- Text and voice input
- Severity engine with Low, Medium, High, Emergency levels
- Hospital alerts with navigation links
- User dashboard with history and recurring symptom tracking
- Admin dashboard for trends, hotspots, and language analytics

## Technology Stack
**Frontend:** Angular 17, Chart.js, Web Speech API  
**Backend:** Spring Boot 3, Spring Security, JPA  
**AI Engine:** Gemini API  
**Database:** MySQL 8 (UTF8MB4)  
**Alerts:** SMS Gateway  

## How It Works
1. User submits symptoms (text or voice)  
2. AI generates a short, simple explanation  
3. Severity engine assigns a risk level  
4. High-severity cases trigger hospital alerts  
5. Data updates both user analytics and admin dashboards  

## Real-World Use Cases
- ASHA and field health workers
- Telemedicine triage
- NGO health programs
- School health screening
- Families using basic smartphones

## Admin Dashboard
- Total users and active users
- State-wise usage
- Language-wise performance
- Severity heatmaps and hotspots
- Symptom trends and spikes

## Repository
GitHub: https://github.com/kunalk276/HealthBridge_Application.git  
Demo Video: https://drive.google.com/file/d/1dCMbMZmeeY7cwiOJJmgMEx7AwS08U_w_/view?usp=sharing
