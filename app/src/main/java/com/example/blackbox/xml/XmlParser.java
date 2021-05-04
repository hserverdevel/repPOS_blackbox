package com.example.blackbox.xml;

/**
 * Created by tiziano on 12/14/18.
 */

import android.os.Environment;

import com.example.blackbox.client.SendFileThread;
import com.example.blackbox.model.CashButtonLayout;
import com.example.blackbox.model.CashButtonListLayout;
import com.example.blackbox.model.ClientInfo;
import com.example.blackbox.model.DeviceInfo;
import com.example.blackbox.model.StaticValue;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XmlParser {

    public static double roundDecimal(double floatNum, int numberOfDecimals) {
        BigDecimal value = new BigDecimal(floatNum);
        value = value.setScale(numberOfDecimals, RoundingMode.HALF_EVEN);
        return value.doubleValue();
    }

    public void createXml(
            ClientInfo clientInfo,
            ArrayList<CashButtonLayout> products,
            Map<CashButtonLayout,ArrayList<CashButtonListLayout>> modifiers,
            int numeroFattura,
            int metodoPagamento

    ){

        DeviceInfo deviceInfo = StaticValue.deviceInfo;
        Map<Double, Double> ivaCount = new HashMap<Double, Double>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();


            Element rootElement = doc.createElement("p:FatturaElettronica");
            doc.appendChild(rootElement);

            Attr rootAttr1 = doc.createAttribute("versione");
            rootAttr1.setValue("FPR12");
            rootElement.setAttributeNode(rootAttr1);

            Attr rootAttr2 = doc.createAttribute("xmlns:ds");
            rootAttr2.setValue("http://www.w3.org/2000/09/xmldsig#");
            rootElement.setAttributeNode(rootAttr2);

            Attr rootAttr3 = doc.createAttribute("xmlns:p");
            rootAttr3.setValue("http://ivaservizi.agenziaentrate.gov.it/docs/xsd/fatture/v1.2");
            rootElement.setAttributeNode(rootAttr3);

            Attr rootAttr4 = doc.createAttribute("xmlns:xsi");
            rootAttr4.setValue("http://www.w3.org/2001/XMLSchema-instance");
            rootElement.setAttributeNode(rootAttr4);


            Attr rootAttr5 = doc.createAttribute("xsi:schemaLocation");
            rootAttr5.setValue("http://ivaservizi.agenziaentrate.gov.it/docs/xsd/fatture/v1.2 http://www.fatturapa.gov.it/export/fatturazione/sdi/fatturapa/v1.2/Schema_del_file_xml_FatturaPA_versione_1.2.xsd");
            rootElement.setAttributeNode(rootAttr5);

            //FATTURA ELETTRONICA HEADER

            Element FattureElettronicaHeader = doc.createElement("FatturaElettronicaHeader");
            rootElement.appendChild(FattureElettronicaHeader);

            //DATI TRASMISSIONE

            Element DatiTrasmissione = doc.createElement("DatiTrasmissione");
            FattureElettronicaHeader.appendChild(DatiTrasmissione);

            Element IdTrasmittente = doc.createElement("IdTrasmittente");
            DatiTrasmissione.appendChild(IdTrasmittente);

            Element IdPaese = doc.createElement("IdPaese");
            IdPaese.appendChild(doc.createTextNode("IT"));
            IdTrasmittente.appendChild(IdPaese);

            Element IdCodice = doc.createElement("IdCodice");
            //TODO da fare appena mi danno il numero
            //IdCodice.appendChild(doc.createTextNode("10147830011"));
            IdCodice.appendChild(doc.createTextNode(deviceInfo.getPartitaIva()));
            IdTrasmittente.appendChild(IdCodice);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            Element ProgressivoInvio = doc.createElement("ProgressivoInvio");
            ProgressivoInvio.appendChild(doc.createTextNode(StaticValue.shopName.toUpperCase()+cal.get(Calendar.YEAR)+numeroFattura));
            DatiTrasmissione.appendChild(ProgressivoInvio);

            Element FormatoTrasmissione = doc.createElement("FormatoTrasmissione");
            FormatoTrasmissione.appendChild(doc.createTextNode("FPR12"));
            DatiTrasmissione.appendChild(FormatoTrasmissione);

            if(clientInfo.getCodice_destinatario().equals("")){
                Element CodiceDestinatario = doc.createElement("CodiceDestinatario");
                CodiceDestinatario.appendChild(doc.createTextNode("0000000"));
                DatiTrasmissione.appendChild(CodiceDestinatario);

                Element Pec = doc.createElement("PECDestinatario");
                Pec.appendChild(doc.createTextNode(clientInfo.getPec()));
                DatiTrasmissione.appendChild(Pec);
            }else{
                Element CodiceDestinatario = doc.createElement("CodiceDestinatario");
                CodiceDestinatario.appendChild(doc.createTextNode(clientInfo.getCodice_destinatario()));
                DatiTrasmissione.appendChild(CodiceDestinatario);
            }

           /* Element CodiceDestinatario = doc.createElement("CodiceDestinatario");
            CodiceDestinatario.appendChild(doc.createTextNode("M5UXCR1"));
            DatiTrasmissione.appendChild(CodiceDestinatario);*/

            //CEDENTE PRESTATORE

            Element CedentePrestatore = doc.createElement("CedentePrestatore");
            FattureElettronicaHeader.appendChild(CedentePrestatore);

            Element DatiAnagrafici = doc.createElement("DatiAnagrafici");
            CedentePrestatore.appendChild(DatiAnagrafici);

            Element IdFiscaleIVA = doc.createElement("IdFiscaleIVA");
            DatiAnagrafici.appendChild(IdFiscaleIVA);

            Element IdPaeseC = doc.createElement("IdPaese");
            IdPaeseC.appendChild(doc.createTextNode("IT"));
            IdFiscaleIVA.appendChild(IdPaeseC);

            Element IdCodiceC = doc.createElement("IdCodice");
            IdCodiceC.appendChild(doc.createTextNode(deviceInfo.getPartitaIva()));
            //IdCodiceC.appendChild(doc.createTextNode("10147830011"));
            IdFiscaleIVA.appendChild(IdCodiceC);


            Element Anagrafica = doc.createElement("Anagrafica");
            DatiAnagrafici.appendChild(Anagrafica);

            Element DenominazioneC = doc.createElement("Denominazione");
            //DenominazioneC.appendChild(doc.createTextNode("GIRLS IN GARDEN SRL"));
            DenominazioneC.appendChild(doc.createTextNode(deviceInfo.getRagioneSociale().toUpperCase()));
            Anagrafica.appendChild(DenominazioneC);

            Element RegimeFiscale = doc.createElement("RegimeFiscale");
            RegimeFiscale.appendChild(doc.createTextNode("RF01"));
            DatiAnagrafici.appendChild(RegimeFiscale);

            Element SedeC = doc.createElement("Sede");
            CedentePrestatore.appendChild(SedeC);

            Element IndirizzoC = doc.createElement("Indirizzo");
            IndirizzoC.appendChild(doc.createTextNode(deviceInfo.getAddress().toUpperCase()));
            //IndirizzoC.appendChild(doc.createTextNode("VIA VALPRATO 68"));
            SedeC.appendChild(IndirizzoC);

            Element CAPC = doc.createElement("CAP");
            CAPC.appendChild(doc.createTextNode(String.valueOf(deviceInfo.getCap())));
            //CAPC.appendChild(doc.createTextNode("10155"));
            SedeC.appendChild(CAPC);

            Element ComuneC = doc.createElement("Comune");
            ComuneC.appendChild(doc.createTextNode(deviceInfo.getComune().toUpperCase()));
            //ComuneC.appendChild(doc.createTextNode("TORINO"));
            SedeC.appendChild(ComuneC);

            Element ProvinciaC = doc.createElement("Provincia");
            ProvinciaC.appendChild(doc.createTextNode("TO"));
            SedeC.appendChild(ProvinciaC);

            Element NazioneC = doc.createElement("Nazione");
            NazioneC.appendChild(doc.createTextNode("IT"));
            SedeC.appendChild(NazioneC);

            //Cessionario Committente

            Element CessionarioCommittente = doc.createElement("CessionarioCommittente");
            FattureElettronicaHeader.appendChild(CessionarioCommittente);

            Element DatiAnagraficiCC = doc.createElement("DatiAnagrafici");
            CessionarioCommittente.appendChild(DatiAnagraficiCC);

            if(clientInfo.getCompany_name().equals("")) {
                Element CodiceFiscaleCC = doc.createElement("CodiceFiscale");
                CodiceFiscaleCC.appendChild(doc.createTextNode(clientInfo.getCodice_fiscale().toUpperCase()));
                DatiAnagraficiCC.appendChild(CodiceFiscaleCC);


                Element AnagraficaCC = doc.createElement("Anagrafica");
                DatiAnagraficiCC.appendChild(AnagraficaCC);

                Element DenominazioneCC = doc.createElement("Denominazione");
                DenominazioneCC.appendChild(doc.createTextNode(clientInfo.getName().toUpperCase()+" " + clientInfo.getSurname().toUpperCase()));
                AnagraficaCC.appendChild(DenominazioneCC);
            }else{
                Element CodiceFiscaleCC = doc.createElement("CodiceFiscale");
                CodiceFiscaleCC.appendChild(doc.createTextNode(clientInfo.getCompany_vat_number().toUpperCase()));
                DatiAnagraficiCC.appendChild(CodiceFiscaleCC);


                Element AnagraficaCC = doc.createElement("Anagrafica");
                DatiAnagraficiCC.appendChild(AnagraficaCC);

                Element DenominazioneCC = doc.createElement("Denominazione");
                DenominazioneCC.appendChild(doc.createTextNode(clientInfo.getCompany_name().toUpperCase()));
                AnagraficaCC.appendChild(DenominazioneCC);
            }

            Element SedeCC = doc.createElement("Sede");
            CessionarioCommittente.appendChild(SedeCC);

            Element IndirizzoCC = doc.createElement("Indirizzo");
            IndirizzoCC.appendChild(doc.createTextNode(clientInfo.getCompany_address().toUpperCase()));
            SedeCC.appendChild(IndirizzoCC);

            Element CAPCC = doc.createElement("CAP");
            CAPCC.appendChild(doc.createTextNode(clientInfo.getCompany_postal_code()));
            SedeCC.appendChild(CAPCC);

            Element ComuneCC = doc.createElement("Comune");
            ComuneCC.appendChild(doc.createTextNode(clientInfo.getCompany_city().toUpperCase()));
            SedeCC.appendChild(ComuneCC);

            //TODO INSERT PROVINCIA IN CLIENT
            Element ProvinciaCC = doc.createElement("Provincia");
            ProvinciaCC.appendChild(doc.createTextNode(clientInfo.getProvincia().toUpperCase()));
            SedeCC.appendChild(ProvinciaCC);

            Element NazioneCC = doc.createElement("Nazione");
            NazioneCC.appendChild(doc.createTextNode("IT"));
            SedeCC.appendChild(NazioneCC);


            //FATTURA ELETTRONICA BODY

            Element FatturaElettronicaBody = doc.createElement("FatturaElettronicaBody");
            rootElement.appendChild(FatturaElettronicaBody);

            //DATI GENERALI

            Element DatiGenerali = doc.createElement("DatiGenerali");
            FatturaElettronicaBody.appendChild(DatiGenerali);

            //DATI GENERALI DOCUMENTO

            Element DatiGeneraliDocumento = doc.createElement("DatiGeneraliDocumento");
            DatiGenerali.appendChild(DatiGeneraliDocumento);

            Element TipoDocumento = doc.createElement("TipoDocumento");
            TipoDocumento.appendChild(doc.createTextNode("TD01"));
            DatiGeneraliDocumento.appendChild(TipoDocumento);

            Element Divisa = doc.createElement("Divisa");
            Divisa.appendChild(doc.createTextNode("EUR"));
            DatiGeneraliDocumento.appendChild(Divisa);


            Element Data = doc.createElement("Data");
            Data.appendChild(doc.createTextNode(dateFormat.format(date)));
            DatiGeneraliDocumento.appendChild(Data);

            Element Numero = doc.createElement("Numero");
            Numero.appendChild(doc.createTextNode(StaticValue.shopName.toUpperCase()+"-"+String.valueOf(numeroFattura)));
            DatiGeneraliDocumento.appendChild(Numero);

            Element Causale = doc.createElement("Causale");
            Causale.appendChild(doc.createTextNode("FATTURA PER PASTO CONSUMATO"));
            DatiGeneraliDocumento.appendChild(Causale);

            //dati ordine acquisto

            /*Element DatiOrdineAcquisto = doc.createElement("DatiOrdineAcquisto");
            DatiGenerali.appendChild(DatiOrdineAcquisto);

            Element RiferimentoNumeroLinea = doc.createElement("RiferimentoNumeroLinea");
            RiferimentoNumeroLinea.appendChild(doc.createTextNode("1"));
            DatiOrdineAcquisto.appendChild(RiferimentoNumeroLinea);

            Element IdDocumento = doc.createElement("IdDocumento");
            IdDocumento.appendChild(doc.createTextNode("66685"));
            DatiOrdineAcquisto.appendChild(IdDocumento);

            Element NumItem = doc.createElement("NumItem");
            NumItem.appendChild(doc.createTextNode("1"));
            DatiOrdineAcquisto.appendChild(NumItem);*/

            //DATI BENI SERVIZI

            Element DatiBeniServizi = doc.createElement("DatiBeniServizi");
            FatturaElettronicaBody.appendChild(DatiBeniServizi);

            //DETTAGLIO LINEE



            int i = 0;
            for(CashButtonLayout product : products) {
                Element DettaglioLinee = doc.createElement("DettaglioLinee");
                //DatiBeniServizi.appendChild(DettaglioLinee);

                i++;
                Element NumeroLinea = doc.createElement("NumeroLinea");
                NumeroLinea.appendChild(doc.createTextNode(String.valueOf(i)));
                DettaglioLinee.appendChild(NumeroLinea);

                Element Descrizione = doc.createElement("Descrizione");
                Descrizione.appendChild(doc.createTextNode(product.getTitle().toUpperCase()));
                DettaglioLinee.appendChild(Descrizione);

                Element Quantita = doc.createElement("Quantita");
                Quantita.appendChild(doc.createTextNode(String.valueOf(product.getQuantity())+".00"));
                DettaglioLinee.appendChild(Quantita);

                double iva = (double) StaticValue.vats[product.getVat()];
                double toApplay = 1+(iva/100);
                double prezzoUnitario = product.getPriceFloat()/toApplay;

                Element PrezzoUnitario = doc.createElement("PrezzoUnitario");
                PrezzoUnitario.appendChild(doc.createTextNode(String.format("%.2f", roundDecimal(prezzoUnitario, 2)).replace(",", ".")));
                DettaglioLinee.appendChild(PrezzoUnitario);

                Element PrezzoTotale = doc.createElement("PrezzoTotale");
                PrezzoTotale.appendChild(doc.createTextNode(String.format("%.2f", roundDecimal(prezzoUnitario*product.getQuantityInt(), 2)).replace(",", ".")));
                DettaglioLinee.appendChild(PrezzoTotale);

                Element AliquotaIVA = doc.createElement("AliquotaIVA");
                AliquotaIVA.appendChild(doc.createTextNode(String.format("%.2f",iva).replace(",", ".")));
                DettaglioLinee.appendChild(AliquotaIVA);

                if(ivaCount.containsKey(iva)){
                    double oldPrezzo = ivaCount.get(iva);
                    ivaCount.put(iva, oldPrezzo+(prezzoUnitario*product.getQuantityInt()));
                }else{
                    ivaCount.put(iva, (prezzoUnitario*product.getQuantityInt()));
                }

                DatiBeniServizi.appendChild(DettaglioLinee);


                ArrayList<CashButtonListLayout> mList = modifiers.get(product);
                if (mList != null) {
                    for (CashButtonListLayout m : mList) {
                        if (m.getPriceFloat() == 0) continue;
                        else {
                            i++;

                            Element DettaglioLineeInside = doc.createElement("DettaglioLinee");
                            //DatiBeniServizi.appendChild(DettaglioLinee);

                            Element NumeroLineaMod = doc.createElement("NumeroLinea");
                            NumeroLineaMod.appendChild(doc.createTextNode(String.valueOf(i)));
                            DettaglioLineeInside.appendChild(NumeroLineaMod);

                            Element DescrizioneMod = doc.createElement("Descrizione");
                            DescrizioneMod.appendChild(doc.createTextNode(m.getTitle().toUpperCase()));
                            DettaglioLineeInside.appendChild(DescrizioneMod);

                            Element QuantitaMod = doc.createElement("Quantita");
                            QuantitaMod.appendChild(doc.createTextNode(String.valueOf(m.getQuantity())+".00"));
                            DettaglioLineeInside.appendChild(QuantitaMod);

                            double ivaMod =  StaticValue.vats[m.getVat()];
                            double toApplayMod = 1+(ivaMod/100);
                            double prezzoUnitarioMod = m.getPriceFloat()/toApplayMod;

                            Element PrezzoUnitarioMod = doc.createElement("PrezzoUnitario");
                            PrezzoUnitarioMod.appendChild(doc.createTextNode(String.format("%.2f", roundDecimal(prezzoUnitarioMod, 2)).replace(",", ".")));
                            DettaglioLineeInside.appendChild(PrezzoUnitarioMod);

                            Element PrezzoTotaleMod = doc.createElement("PrezzoTotale");
                            PrezzoTotaleMod.appendChild(doc.createTextNode(String.format("%.2f", roundDecimal(prezzoUnitarioMod*m.getQuantityInt(), 2)).replace(",", ".")));
                            DettaglioLineeInside.appendChild(PrezzoTotaleMod);

                            Element AliquotaIVAMod = doc.createElement("AliquotaIVA");
                            AliquotaIVAMod.appendChild(doc.createTextNode(String.format("%.2f",ivaMod).replace(",", ".")));
                            DettaglioLineeInside.appendChild(AliquotaIVAMod);

                            if(ivaCount.containsKey(ivaMod)){
                                double oldPrezzo = ivaCount.get(ivaMod);
                                ivaCount.put(ivaMod, oldPrezzo+(prezzoUnitarioMod*m.getQuantityInt()));
                            }else{
                                ivaCount.put(ivaMod, (prezzoUnitarioMod*m.getQuantityInt()));
                            }

                            DatiBeniServizi.appendChild(DettaglioLineeInside);
                        }
                    }
                }

            }

            //DATI RIEPILOGO

            double totale = 0.0;

            for (Map.Entry<Double, Double> entry : ivaCount.entrySet()) {

                Element DatiRiepilogo = doc.createElement("DatiRiepilogo");
                DatiBeniServizi.appendChild(DatiRiepilogo);

                Element AliquotaIVAR = doc.createElement("AliquotaIVA");
                AliquotaIVAR.appendChild(doc.createTextNode(String.format("%.2f",entry.getKey()).replace(",", ".")));
                DatiRiepilogo.appendChild(AliquotaIVAR);

                Element ImponibileImporto = doc.createElement("ImponibileImporto");
                ImponibileImporto.appendChild(doc.createTextNode(String.format("%.2f",entry.getValue()).replace(",", ".")));
                DatiRiepilogo.appendChild(ImponibileImporto);

                double imposta = roundDecimal((entry.getValue()*entry.getKey())/100, 2);

                totale += roundDecimal(entry.getValue()+imposta,2);

                Element Imposta = doc.createElement("Imposta");
                Imposta.appendChild(doc.createTextNode(String.format("%.2f",imposta).replace(",", ".")));
                DatiRiepilogo.appendChild(Imposta);

                Element EsigibilitaIVA = doc.createElement("EsigibilitaIVA");
                EsigibilitaIVA.appendChild(doc.createTextNode("I"));
                DatiRiepilogo.appendChild(EsigibilitaIVA);
            }

            //DatiPagamento

            Element DatiPagamento = doc.createElement("DatiPagamento");
            FatturaElettronicaBody.appendChild(DatiPagamento);

            Element CondizioniPagamento = doc.createElement("CondizioniPagamento");
            CondizioniPagamento.appendChild(doc.createTextNode("TP01"));
            DatiPagamento.appendChild(CondizioniPagamento);

            Element DettaglioPagamento = doc.createElement("DettaglioPagamento");
            DatiPagamento.appendChild(DettaglioPagamento);

            Element ModalitaPagamento = doc.createElement("ModalitaPagamento");
            if(metodoPagamento==1)
                ModalitaPagamento.appendChild(doc.createTextNode("MP01"));
            else
                ModalitaPagamento.appendChild(doc.createTextNode("MP08"));
            DettaglioPagamento.appendChild(ModalitaPagamento);

            Element DataScadenzaPagamento = doc.createElement("DataScadenzaPagamento");
            DataScadenzaPagamento.appendChild(doc.createTextNode(dateFormat.format(date)));
            DettaglioPagamento.appendChild(DataScadenzaPagamento);

            Element ImportoPagamento = doc.createElement("ImportoPagamento");
            ImportoPagamento.appendChild(doc.createTextNode(String.format("%.2f",totale).replace(",", ".")));
            DettaglioPagamento.appendChild(ImportoPagamento);





            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            DOMSource source = new DOMSource(doc);
            int year = Calendar.getInstance().get(Calendar.YEAR);
            String nomeFile = "IT10147830011_FPR12_"+StaticValue.shopName+String.valueOf(year)+numeroFattura+".xml";
            File myFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    nomeFile);
            StreamResult result = new StreamResult(myFile);
            //StreamResult result = new StreamResult(new File("invoice.xml"));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

            SendFileThread sft = new SendFileThread();
            sft.setFileName(nomeFile);
            sft.execute(nomeFile);
            sft.setFileName("");
            //myFile.delete();




            System.out.println("File saved!");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }

    }




}
