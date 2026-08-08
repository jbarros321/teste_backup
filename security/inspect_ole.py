import olefile

file_path = r'e:\personalizacoes-main\personalizacoes-main\security\Eventos - Mar 2026 (1).xlsx'

try:
    if olefile.isOleFile(file_path):
        ole = olefile.OleFileIO(file_path)
        print("Streams encontrados:")
        for stream in ole.listdir():
            print("/".join(stream))
        
        # Check for IRM signatures
        if ole.exists("\x06DataSpaces"):
            print("Detectado DataSpaces - Provavelmente IRM (Azure RMS).")
        if ole.exists("EncryptionInfo"):
            print("Detectado EncryptionInfo - Arquivo Criptografado com Senha ou IRM.")
    else:
        print("Não é um arquivo OLE (não é Legacy nem IRM-wrapped).")
except Exception as e:
    print(f"Erro: {e}")
