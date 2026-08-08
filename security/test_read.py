import pandas as pd
import msoffcrypto
import io
import xlrd
import sys

file_path = r'e:\personalizacoes-main\personalizacoes-main\security\Eventos - Mar 2026 (1).xlsx'

def try_read():
    try:
        # Try msoffcrypto
        with open(file_path, "rb") as f:
            file = msoffcrypto.OfficeFile(f)
            if file.is_encrypted():
                print("O arquivo está criptografado (talvez IRM).")
                # We can't decrypt without password/keys
                return
        
        # Try xlrd (for legacy format)
        df = pd.read_excel(file_path, engine='xlrd')
        print("Sucesso ao ler o arquivo com xlrd!")
        print(df.head())
        return
    except Exception as e:
        print(f"Erro ao ler o arquivo: {e}")

if __name__ == "__main__":
    try_read()
