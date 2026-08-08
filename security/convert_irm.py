import win32com.client
import os
import time

file_path = r'e:\personalizacoes-main\personalizacoes-main\security\Eventos - Mar 2026 (1).xlsx'
output_path = r'e:\personalizacoes-main\personalizacoes-main\security\Eventos_Mar_2026_Convertido.csv'

def convert():
    excel = None
    try:
        print("Iniciando Excel via COM...")
        excel = win32com.client.Dispatch("Excel.Application")
        excel.Visible = False
        excel.DisplayAlerts = False
        
        print(f"Abrindo arquivo: {file_path}")
        # Opening an IRM file usually works if the user is authenticated in the desktop app
        wb = excel.Workbooks.Open(file_path)
        
        print(f"Salvando como CSV: {output_path}")
        # xlCSV = 6
        wb.SaveAs(output_path, FileFormat=6)
        wb.Close()
        print("Conversão concluída com sucesso!")
        
    except Exception as e:
        print(f"Erro durante a conversão: {e}")
    finally:
        if excel:
            try:
                excel.Quit()
            except:
                pass

if __name__ == "__main__":
    convert()
