#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#define DATA_SIZE 25
#define MAX 1024
int String_to_Int(char* input);
typedef struct t
{
	int hour;
	int minute;
	int second;
	int milisecond;
}time;
int main(void)
{
	time pixel5_time[DATA_SIZE];
	time pixel4_time[DATA_SIZE];
	char pixel5_string[DATA_SIZE][MAX];
	char pixel4_string[DATA_SIZE][MAX];
	int pixel5_total = 0;
	int pixel4_total = 0;
	double pixel5_mean = 0.0;
	double pixel4_mean = 0.0;
	char temp[MAX];
	char* denom = ":.";
	FILE * pixel5_file = fopen("./pixel5.txt","rt");
	FILE * pixel4_file = fopen("./pixel4.txt","rt");
	//read pixel5 file
	for(int i = 0; i< DATA_SIZE;i++)
	{
		fscanf(pixel5_file,"%s",pixel5_string[i]);		
	}
	for(int i = 0; i< DATA_SIZE;i++)
	{
		fscanf(pixel4_file,"%s",pixel4_string[i]);		
	}
	//calculate time
	for(int i = 0; i< DATA_SIZE;i++)
	{
		strcpy(temp,pixel5_string[i]);
		char* tok; 
		tok = strtok(temp,denom);
		pixel5_time[i].hour = String_to_Int(tok);
		tok = strtok(NULL,denom);
		pixel5_time[i].minute = String_to_Int(tok);
		tok = strtok(NULL,denom);
		pixel5_time[i].second = String_to_Int(tok);
		tok = strtok(NULL,denom);
		pixel5_time[i].milisecond = String_to_Int(tok);
	}
	for(int i = 0; i< DATA_SIZE;i++)
	{
		strcpy(temp,pixel4_string[i]);
		char* tok;
		tok = strtok(temp,denom);
		pixel4_time[i].hour = String_to_Int(tok);
		tok = strtok(NULL,denom);
		pixel4_time[i].minute = String_to_Int(tok);
		tok = strtok(NULL,denom);
		pixel4_time[i].second = String_to_Int(tok);
		tok = strtok(NULL,denom);
		pixel4_time[i].milisecond = String_to_Int(tok);
	}
	//calculate mean
	for(int i = 0; i<DATA_SIZE;i++)
	{
		printf("pixel5[%d] %dh : %dm : %ds : %dms\n",i,pixel5_time[i].hour,pixel5_time[i].minute,pixel5_time[i].second,pixel5_time[i].milisecond);
		pixel5_total += (pixel5_time[i].hour * 3600000) + (pixel5_time[i].minute * 60000) + (pixel5_time[i].second * 1000) + pixel5_time[i].milisecond;
	}

	pixel5_mean = (double)pixel5_total / DATA_SIZE;
	printf("\n");
	for(int i = 0; i<DATA_SIZE;i++)
	{
		printf("pixel4[%d] %dh : %dm : %ds : %dms\n",i,pixel4_time[i].hour,pixel4_time[i].minute,pixel4_time[i].second,pixel4_time[i].milisecond);
		pixel4_total += (pixel4_time[i].hour * 3600000) + (pixel4_time[i].minute * 60000) + (pixel4_time[i].second * 1000) + pixel4_time[i].milisecond;
	}
	pixel4_mean = (double)pixel4_total / DATA_SIZE;
	printf("pixel5 : %lf\npixel4 : %lf\npixel5-pixel4 = %lf\n",pixel5_mean,pixel4_mean,pixel5_mean-pixel4_mean);
}

int String_to_Int(char* input)
{
	int len = strlen(input);
	int result=0;
	if(len < 2)
	{
		result = atoi(input);
	}
	else
	{
		char temp[3];
		strncpy(temp,input,3);
		result = atoi(temp);
	}
	return result;
}

